package com.example.notes.data;

import android.support.annotation.NonNull;

import com.example.notes.domain.Note;
import com.example.notes.domain.RemoteRepository;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class RemoteRepositoryImpl implements RemoteRepository {
    private static final String USER_NAME = "USER_NAME_V7";
    @NonNull
    private final NotesApi notesApi;

    public RemoteRepositoryImpl(@NonNull NotesApi notesApi) {
        this.notesApi = notesApi;
    }

    @NonNull
    public Observable<Note> syncNotes(@NonNull List<Note> notes) {
        NotesApi.NotesRequestBody notesRequestBody = new NotesApi.NotesRequestBody(0, USER_NAME, notes);
        return notesApi.syncNotes(notesRequestBody)
                .flatMap(notesResponseBody -> Observable.fromIterable(notesResponseBody.notes))
                .subscribeOn(Schedulers.io());
    }
}
