package com.example.notes.data;

import android.support.annotation.NonNull;

import com.example.notes.domain.LocalRepository;
import com.example.notes.domain.Note;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class LocalRepositoryImpl implements LocalRepository {
    @NonNull
    private final NotesDao notesDao;

    public LocalRepositoryImpl(@NonNull NotesDao notesDao) {
        this.notesDao = notesDao;
    }

    @NonNull
    public Single<List<Note>> getAllNotes() {
        return Single.fromCallable(notesDao::getAllNotes)
                .subscribeOn(Schedulers.io());
    }

    @NonNull
    public Single<Note> getNote(@NonNull String guid) {
        return Single.fromCallable(() -> notesDao.getNote(guid))
                .subscribeOn(Schedulers.io());
    }

    @NonNull
    public Single<Note> addNote(@NonNull Note note) {
        return Single.fromCallable(() -> notesDao.addNote(note))
                .subscribeOn(Schedulers.io());
    }

    @NonNull
    public Single<Note> updateNote(@NonNull Note note) {
        return Single.fromCallable(() -> notesDao.updateNote(note))
                .subscribeOn(Schedulers.io());
    }

    @NonNull
    public Single<Note> deleteNote(@NonNull String guid) {
        return Single.fromCallable(() -> notesDao.deleteNote(guid))
                .subscribeOn(Schedulers.io());
    }

    @NonNull
    public Completable syncNotes(@NonNull List<Note> notes) {
        return Completable.fromAction(() -> notesDao.syncNotes(notes))
                .subscribeOn(Schedulers.io());
    }
}
