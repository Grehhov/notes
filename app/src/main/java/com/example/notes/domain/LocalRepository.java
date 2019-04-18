package com.example.notes.domain;

import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface LocalRepository {
    Single<List<Note>> getAllNotes();

    @NonNull
    Single<Note> getNote(@NonNull String guid);

    @NonNull
    Single<Note> addNote(@NonNull Note note);

    @NonNull
    Single<Note> updateNote(@NonNull Note note);

    @NonNull
    Single<Note> deleteNote(@NonNull Note note);

    @NonNull
    Completable syncNotes(@NonNull List<Note> notes);
}
