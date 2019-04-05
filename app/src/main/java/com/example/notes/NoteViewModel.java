package com.example.notes;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

/**
 * Управляет заметкой
 */
public class NoteViewModel extends ViewModel implements NotesRepository.NotesSynchronizedListener {
    private static final String TAG = "NoteViewModel";
    @NonNull
    private final NotesRepository notesRepository;
    @NonNull
    private final MutableLiveData<Note> note = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> exitOnSync = new MutableLiveData<>();

    public NoteViewModel() {
        this.notesRepository = NotesRepository.getInstance();
        this.notesRepository.addNotesSynchronizedListener(this);
    }

    void setIndex(int index) {
        if (index >= 0) {
            try {
                note.setValue(notesRepository.getNote(index).clone());
            } catch (CloneNotSupportedException e) {
                Log.d(TAG,"Object cloning error", e);
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

    @NonNull
    LiveData<Boolean> getExitOnSync() {
        return exitOnSync;
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
            isRefreshing.setValue(true);
            notesRepository.updateNote(note.getValue());
        }
    }

    void addNote() {
        if (note.getValue() != null) {
            isRefreshing.setValue(true);
            notesRepository.addNote(note.getValue());
        }
    }

    void deleteNote() {
        if (note.getValue() != null) {
            isRefreshing.setValue(true);
            notesRepository.deleteNote(notesRepository.getNote(note.getValue().getId()));
        }
    }

    @Override
    public void onSynchronized(@NonNull List<Note> notes) {
        if (Boolean.TRUE.equals(isRefreshing.getValue())) {
            exitOnSync.setValue(true);
            isRefreshing.setValue(false);
        }
    }

    @Override
    public void onError() {
        isRefreshing.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        notesRepository.removeNotesSynchronizedListener(this);
    }
}
