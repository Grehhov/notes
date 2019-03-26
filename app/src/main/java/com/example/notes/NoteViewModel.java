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

    void updateNote(int id, @NonNull Note note) {
        notesRepository.updateNote(id, note);
    }

    void addNote(@NonNull Note note) {
        notesRepository.addNote(note);
    }
}
