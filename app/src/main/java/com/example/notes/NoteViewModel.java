package com.example.notes;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Управляет заметкой
 */
public class NoteViewModel extends ViewModel implements NotesRepository.NotesRefreshListener {
    @NonNull
    private final NotesRepository notesRepository;
    @NonNull
    private final MutableLiveData<Note> note = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>();

    public NoteViewModel() {
        this.notesRepository = NotesRepository.getInstance();
        this.notesRepository.addNotesRefreshListener(this);
    }

    void setIndex(int index) {
        if (index >= 0) {
            try {
                note.setValue(notesRepository.getNote(index).clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        } else {
            note.setValue(new Note(notesRepository.getUniqueId(), "", null));
        }
    }

    @NonNull
    LiveData<Note> getNote() {
        return note;
    }

    @NonNull
    LiveData<Boolean> getIsRefreshing() {
        return isRefreshing;
    }

    void saveNoteInfo(@NonNull String name, @Nullable String description) {
        Note note = this.note.getValue();
        if (note != null) {
            note.setName(name);
            note.setDescription(description);
            this.note.setValue(note);
        }
    }

    void updateNote() {
        if (note.getValue() != null) {
            notesRepository.updateNote(note.getValue());
        }
    }

    void addNote() {
        if (note.getValue() != null) {
            notesRepository.addNote(note.getValue());
        }
    }

    void deleteNote() {
        if (note.getValue() != null) {
            notesRepository.deletedNote(notesRepository.getNote(note.getValue().getId()));
        }
    }

    @Override
    public void onStartRefresh() {
        isRefreshing.setValue(true);
    }

    @Override
    public void onCompleteRefresh(@NonNull List<Note> notes) {
        isRefreshing.setValue(false);
    }

    @Override
    public void onError() {
        isRefreshing.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        notesRepository.removeNotesRefreshListener(this);
    }
}
