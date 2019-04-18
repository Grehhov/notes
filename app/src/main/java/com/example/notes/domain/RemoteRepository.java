package com.example.notes.domain;

import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.Observable;

public interface RemoteRepository {
    @NonNull
    Observable<Note> syncNotes(@NonNull List<Note> notes);
}
