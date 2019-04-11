package com.example.notes;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Предоставляет возможность взаимодействия с базой данных SQLite в фоновом потоке
 */
class AsyncSqliteHelper {
    static class LoadNotesTask extends AsyncTask<Void, Void, List<Note>> {
        @NonNull
        private final NotesRepository repository;

        LoadNotesTask(@NonNull NotesRepository repository) {
            this.repository = repository;
        }

        @Nullable
        @Override
        protected List<Note> doInBackground(Void... voids) {
            return repository.getAllNotesDb();
        }

        @Override
        public void onPostExecute(@Nullable List<Note> notes) {
            if (notes != null) {
                repository.updateNotes(notes);
            } else {
                repository.notifyOnError();
            }
        }
    }

    private static abstract class BaseNoteTask extends AsyncTask<Note, Void, Note> {
        @NonNull
        private NotesRepository repository;

        BaseNoteTask(@NonNull NotesRepository repository) {
            this.repository = repository;
        }

        @Override
        public void onPostExecute(@Nullable Note note) {
            if (note != null) {
                repository.updateNotes(note);
            } else {
                repository.notifyOnError();
            }
        }
    }

    static class AddNoteTask extends BaseNoteTask {
        AddNoteTask(@NonNull NotesRepository repository) {
            super(repository);
        }

        @Nullable
        @Override
        protected Note doInBackground(@NonNull Note... notes) {
            return super.repository.addNoteDb(notes[0]);
        }
    }

    static class UpdateNoteTask extends BaseNoteTask {
        UpdateNoteTask(@NonNull NotesRepository repository) {
            super(repository);
        }

        @Nullable
        @Override
        protected Note doInBackground(@NonNull Note... notes) {
            return super.repository.updateNoteDb(notes[0]);
        }
    }

    static class DeleteNoteTask extends BaseNoteTask {
        DeleteNoteTask(@NonNull NotesRepository repository) {
            super(repository);
        }

        @Nullable
        @Override
        protected Note doInBackground(@NonNull Note... notes) {
            return super.repository.deleteNoteDb(notes[0]);
        }
    }
}
