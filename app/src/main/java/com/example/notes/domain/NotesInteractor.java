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
import io.reactivex.subjects.BehaviorSubject;

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
    private final BehaviorSubject<List<Note>> notesSubject = BehaviorSubject.create();
    @NonNull
    private final BehaviorSubject<Boolean> syncSubject = BehaviorSubject.create();

    public NotesInteractor(@NonNull LocalRepository localRepository, @NonNull RemoteRepository remoteRepository) {
        this.localRepository = localRepository;
        this.remoteRepository = remoteRepository;
        updateNotes();
    }

    @NonNull
    public Single<Note> getNote(@NonNull String guid) {
        return localRepository.getNote(guid);
    }

    private void updateNotes() {
        localRepository.getAllNotes()
                .subscribe(new DisposableSingleObserver<List<Note>>() {
                    @Override
                    public void onSuccess(@NonNull List<Note> notes) {
                        notesSubject.onNext(notes);
                        syncNotes(notes);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                });
    }

    @NonNull
    public Single<Note> updateNote(@NonNull Note note) {
        return localRepository.updateNote(note)
                .doOnSuccess(localNote -> updateNotes());
    }

    @NonNull
    public Single<Note> addNote(@NonNull Note note) {
        note.setGuid(UUID.randomUUID().toString());
        return localRepository.addNote(note)
                .doOnSuccess(localNote -> updateNotes());
    }

    @NonNull
    public Single<Note> deleteNote(@NonNull String guid) {
        return localRepository.deleteNote(guid)
                .doOnSuccess(localNote -> updateNotes());
    }

    @NonNull
    public Observable<List<Note>> getNotes() {
        return notesSubject;
    }

    @NonNull
    public Observable<Boolean> getSyncNotes() {
        return syncSubject;
    }

    private void syncNotes(@NonNull List<Note> localNotes) {
        if (lastSyncNotes != null && !lastSyncNotes.isDisposed()) {
            lastSyncNotes.dispose();
        }
        HashMap<String, Note> notes = new HashMap<>();
        for (Note note : localNotes) {
            notes.put(note.getGuid(), note);
        }

        lastSyncNotes = remoteRepository.syncNotes(new ArrayList<>())
                .map(remoteNotes -> {
                    for (Note note : remoteNotes) {
                        if (isNeedUpdate(notes, note)) {
                            notes.put(note.getGuid(), note);
                        }
                    }
                    return new ArrayList<>(notes.values());
                })
                .flatMapCompletable(noteList -> remoteRepository.syncNotes(noteList)
                        .ignoreElement()
                        .mergeWith(localRepository.syncNotes(noteList)))
                .subscribe(() -> {
                    notesSubject.onNext(new ArrayList<>(notes.values()));
                    syncSubject.onNext(true);
                }, throwable -> Log.e(TAG, throwable.getMessage(), throwable));
    }

    boolean isNeedUpdate(@NonNull HashMap<String, Note> notes, @NonNull Note remoteNote) {
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
