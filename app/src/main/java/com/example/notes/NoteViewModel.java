package com.example.notes;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

/**
 * Управляет заметкой
 */
public class NoteViewModel extends ViewModel {
    @NonNull
    private final NotesRepository notesRepository;
    @NonNull
    private final MutableLiveData<Note> note = new MutableLiveData<>();

    public NoteViewModel() {
        this.notesRepository = NotesRepository.getInstance();
    }

    void setIndex(int index) {
        if (index >= 0) {
            note.setValue(notesRepository.getNote(index));
        } else {
            note.setValue(null);
        }
    }

    @NonNull
    LiveData<Note> getNote() {
        return note;
    }

    void updateNote(@NonNull Note note) {
        notesRepository.updateNote(note);
    }

    void addNote(@NonNull Note note) {
        notesRepository.addNote(note);
    }

    int getSizeNotes() {
        return this.notesRepository.getNotes().size();
    }
}
