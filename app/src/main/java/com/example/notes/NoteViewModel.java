package com.example.notes;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

/**
 * Управляет заметкой
 */
public class NoteViewModel extends ViewModel {
    @NonNull
    private NotesRepository notesRepository;

    public NoteViewModel() {
        this.notesRepository = NotesRepository.getInstance();
    }

    @NonNull
    Note getNote(int index) {
        return notesRepository.getNote(index);
    }

    void setNote(int id, @NonNull Note note) {
        notesRepository.setNote(id, note);
    }

    void addNote(@NonNull Note note) {
        notesRepository.addNote(note);
    }
}
