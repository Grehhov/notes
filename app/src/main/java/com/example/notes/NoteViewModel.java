package com.example.notes;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Управляет заметкой
 */
public class NoteViewModel extends AndroidViewModel implements NotesRepository.NotesSynchronizedListener {
    private static final String TAG = "NoteViewModel";
    @NonNull
    private final NotesRepository notesRepository;
    @NonNull
    private final MutableLiveData<Note> note = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> exitOnSync = new MutableLiveData<>();

    public NoteViewModel(Application application) {
        super(application);
        this.notesRepository = NotesRepository.getInstance(application);
        this.notesRepository.addNotesSynchronizedListener(this);
    }

    void setGuid(@Nullable String guid) {
        if (guid != null) {
            Note note = notesRepository.getNote(guid);
            if (note != null) {
                try {
                    this.note.setValue(note.clone());
                } catch (CloneNotSupportedException e) {
                    Log.d(TAG, "Object cloning error", e);
                }
            }
        } else {
            note.setValue(new Note());
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
        Note noteValue = note.getValue();
        if (noteValue != null && noteValue.getGuid() != null) {
            isRefreshing.setValue(true);
            Note note = notesRepository.getNote(noteValue.getGuid());
            if (note != null) {
                notesRepository.deleteNote(note);
            }
        }
    }

    @Override
    public void onSynchronized() {
        if (Boolean.TRUE.equals(isRefreshing.getValue())) {
            exitOnSync.setValue(true);
            isRefreshing.setValue(false);
        }
    }

    @Override
    public void onSynchronizedWithNetwork() {

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
