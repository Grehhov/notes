package com.example.notes.data;


import android.support.annotation.NonNull;

import com.example.notes.domain.Note;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NotesApi {
    @POST("/notes/sync")
    Observable<NotesResponseBody> syncNotes(@NonNull @Body NotesRequestBody notesRequestBody);

    class NotesRequestBody {
        public long version;
        @NonNull
        String user;
        @NonNull
        public List<Note> notes;

        NotesRequestBody(long version, @NonNull String user, @NonNull List<Note> notes) {
            this.version = version;
            this.user = user;
            this.notes = notes;
        }
    }

    class NotesResponseBody {
        public long version;
        @NonNull
        public List<Note> notes;

        NotesResponseBody(long version, @NonNull List<Note> notes) {
            this.version = version;
            this.notes = notes;
        }
    }
}