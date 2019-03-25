package com.example.notes;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Содержит операции над хранилищем заметок
 */
public class NotesRepository {
    @NonNull
    private List<Note> notes;
    @Nullable
    private static NotesRepository instance;

    @NonNull
    static NotesRepository getInstance() {
        if (instance == null) {
            instance = new NotesRepository();
        }
        return instance;
    }

    private NotesRepository() {
        notes = new ArrayList<>();
        initNotes();
    }

    private void initNotes() {
        notes.add(new Note("Запись 1", "Описание 1"));
        notes.add(new Note("Запись 2", "Описание 2"));
        notes.add(new Note("Запись 3", "Описание 3"));
        notes.add(new Note("Запись 4", "Описание 4"));
        notes.add(new Note("Запись 5", "Описание 5"));
    }

    @NonNull
    public List<Note> getNotes() {
        return notes;
    }

    @NonNull
    Note getNote(int id) {
        return notes.get(id);
    }

    void setNote(int id, @NonNull Note note) {
        notes.set(id, note);
    }

    void addNote(@NonNull Note note) {
        notes.add(note);
    }
}

