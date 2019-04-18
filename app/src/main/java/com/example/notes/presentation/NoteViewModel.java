package com.example.notes.presentation;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.notes.domain.NotesInteractor;
import com.example.notes.domain.Note;

import javax.inject.Inject;

import io.reactivex.observers.DisposableSingleObserver;

/**
 * Управляет заметкой
 */
public class NoteViewModel extends BaseViewModel {
    private static final String TAG = "NoteViewModel";
    @NonNull
    private final NotesInteractor notesInteractor;
    @NonNull
    private final MutableLiveData<Note> note = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>();
    @NonNull
    private final MutableLiveData<Boolean> exitOnSync = new MutableLiveData<>();
    @NonNull
    private final DisposableSingleObserver<Note> disposableSingleObserver = new DisposableSingleObserver<Note>() {
        @Override
        public void onSuccess(Note note) {
            if (Boolean.TRUE.equals(isRefreshing.getValue())) {
                exitOnSync.postValue(true);
                isRefreshing.postValue(false);
            }
        }

        @Override
        public void onError(Throwable e) {
            isRefreshing.postValue(null);
        }
    };

    @Inject
    NoteViewModel(@NonNull NotesInteractor notesInteractor) {
        this.notesInteractor = notesInteractor;
    }

    void setGuid(@Nullable String guid) {
        if (guid != null) {
            compositeDisposable.add(notesInteractor.getNote(guid)
                    .subscribe(localNote -> {
                        try {
                            note.postValue(localNote.clone());
                        } catch (CloneNotSupportedException e) {
                            Log.d(TAG, "Object cloning error", e);
                        }
                    }));
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
            compositeDisposable.add(notesInteractor.updateNote(note.getValue())
                    .subscribeWith(disposableSingleObserver));
        }
    }

    void addNote() {
        if (note.getValue() != null) {
            isRefreshing.setValue(true);
            compositeDisposable.add(notesInteractor.addNote(note.getValue())
                    .subscribeWith(disposableSingleObserver));
        }
    }

    void deleteNote() {
        Note noteValue = note.getValue();
        if (noteValue != null && noteValue.getGuid() != null) {
            isRefreshing.setValue(true);
            compositeDisposable.add(notesInteractor.deleteNote(noteValue.getGuid())
                    .subscribeWith(disposableSingleObserver));
        }
    }
}
