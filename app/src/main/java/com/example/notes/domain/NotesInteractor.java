package com.example.notes.domain;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.subjects.PublishSubject;

/**
 * Содержит операции над хранилищами заметок
 */
public class NotesInteractor {
    private static final String TAG = "NotesInteractor";
    @Nullable
    private Disposable lastSyncNotes;
    @NonNull
    private final LocalRepository localRepository;
    @NonNull
    private final RemoteRepository remoteRepository;
    @NonNull
    private HashMap<String, Note> notes = new HashMap<>();
    @NonNull
    private final PublishSubject<List<Note>> notesSubject = PublishSubject.create();
    @NonNull
    private final PublishSubject<Boolean> syncSubject = PublishSubject.create();

    public NotesInteractor(@NonNull LocalRepository localRepository, @NonNull RemoteRepository remoteRepository) {
        this.localRepository = localRepository;
        this.remoteRepository = remoteRepository;
        localRepository.getAllNotes()
                .subscribe(new DisposableSingleObserver<List<Note>>() {
                    @Override
                    public void onSuccess(@NonNull List<Note> notes) {
                        updateNotes(notes);
                        dispose();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                });
    }

    @NonNull
    public List<Note> getNotes() {
        return new ArrayList<>(notes.values());
    }

    @NonNull
    public Single<Note> getNote(@NonNull String guid) {
        return localRepository.getNote(guid);
    }

    private void updateNotes(@NonNull List<Note> newNotes) {
        notes = new HashMap<>();
        for (Note note : newNotes) {
            notes.put(note.getGuid(), note);
        }
        notesSubject.onNext(getNotes());
        syncNotes();
    }

    private void updateNotes(@NonNull Note note) {
        notes.put(note.getGuid(), note);
        notesSubject.onNext(getNotes());
        syncNotes();
    }

    @NonNull
    public Single<Note> updateNote(@NonNull Note note) {
        return localRepository.updateNote(note)
                .doOnSuccess(this::updateNotes);
    }

    @NonNull
    public Single<Note> addNote(@NonNull Note note) {
        note.setGuid(UUID.randomUUID().toString());
        return localRepository.addNote(note)
                .doOnSuccess(this::updateNotes);
    }

    @NonNull
    public Single<Note> deleteNote(@NonNull Note note) {
        return localRepository.deleteNote(note)
                .doOnSuccess(this::updateNotes);
    }

    @NonNull
    public Observable<List<Note>> changeNotes() {
        return notesSubject;
    }

    @NonNull
    public Observable<Boolean> getSyncNotes() {
        return syncSubject;
    }

    private void syncNotes() {
        if (lastSyncNotes != null && !lastSyncNotes.isDisposed()) {
            lastSyncNotes.dispose();
        }
        lastSyncNotes = remoteRepository.syncNotes(new ArrayList<>())
                .filter(this::isNeedUpdate)
                .doOnNext(note -> notes.put(note.getGuid(), note))
                .ignoreElements()
                .andThen(remoteRepository.syncNotes(getNotes()).ignoreElements()
                        .mergeWith(localRepository.syncNotes(getNotes())))
                .subscribe(() -> {
                    notesSubject.onNext(getNotes());
                    syncSubject.onNext(true);
                }, throwable -> Log.e(TAG, throwable.getMessage(), throwable));
    }

    private boolean isNeedUpdate(@NonNull Note remoteNote) {
        String guid = remoteNote.getGuid();
        boolean keyIsContains = notes.containsKey(guid);
        boolean isFreshest = false;
        if (keyIsContains) {
            long remoteLastUpdate = remoteNote.getLastUpdate().getTime();
            Note localNote = notes.get(guid);
            if (localNote != null) {
                long localLastUpdate = localNote.getLastUpdate().getTime();
                isFreshest = remoteLastUpdate > localLastUpdate;
            }
        }
        return !keyIsContains || isFreshest;
    }
}
