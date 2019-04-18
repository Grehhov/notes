package com.example.notes.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.example.notes.domain.Note;

import java.util.List;

interface NotesDao {
    @NonNull
    @WorkerThread
    List<Note> getAllNotes();

    @Nullable
    @WorkerThread
    Note getNote(String guid);

    @Nullable
    @WorkerThread
    Note addNote(@NonNull Note note);

    @Nullable
    @WorkerThread
    Note updateNote(@NonNull Note note);

    @Nullable
    @WorkerThread
    Note deleteNote(@NonNull Note note);

    @WorkerThread
    void syncNotes(@NonNull List<Note> notes);
}
