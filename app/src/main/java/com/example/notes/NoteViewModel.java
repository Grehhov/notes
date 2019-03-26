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
    final private NotesRepository notesRepository;

    public NoteViewModel() {
        this.notesRepository = NotesRepository.getInstance();
    }

    @NonNull
    LiveData<Note> getNote(int index) {
        MutableLiveData<Note> note = new MutableLiveData<>();
        if (index >= 0) {
            note.setValue(notesRepository.getNote(index));
        }
        return note;
    }

    void updateNote(int id, @NonNull Note note) {
        notesRepository.updateNote(id, note);
    }

    void addNote(@NonNull Note note) {
        notesRepository.addNote(note);
    }
}
