package com.example.notes;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
            note.setValue(new Note(notesRepository.getNotes().size(), "", null));
        }
    }

    @NonNull
    LiveData<Note> getNote() {
        return note;
    }

    void saveNoteInfo(@NonNull String name, @Nullable String description) {
        Note note = this.note.getValue();
        if (note != null) {
            note.setName(name);
            note.setDescription(description);
            this.note.setValue(note);
        }
    }

    void updateNote(@NonNull Note note) {
        notesRepository.updateNote(note);
    }

    void addNote(@NonNull Note note) {
        notesRepository.addNote(note);
    }
}
