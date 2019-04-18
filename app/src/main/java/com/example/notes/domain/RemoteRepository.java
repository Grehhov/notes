package com.example.notes.domain;

import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.Single;

public interface RemoteRepository {
    @NonNull
    Single<List<Note>> syncNotes(@NonNull List<Note> notes);
}
