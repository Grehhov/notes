package com.example.notes;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.List;

class AsyncSqliteHelper {
    static class GetAllTask extends AsyncTask<Void, Void, List<Note>> {
        @NonNull
        private NotesRepository repository;

        GetAllTask(@NonNull NotesRepository repository) {
            this.repository = repository;
        }

        @Nullable
        @Override
        protected List<Note> doInBackground(Void... voids) {
            return repository.notesDao.getAllNotes();
        }

        @Override
        public void onPostExecute(@Nullable List<Note> notes) {
            if (notes != null) {
                repository.notes = new HashMap<>();
                for (Note note : notes) {
                    repository.notes.put(note.getGuid(), note);
                }
                repository.notifyOnSynchronized();
                new NotesRepository.MergeNotesTask(repository).execute();
            } else {
                repository.notifyOnError();
            }
        }
    }

    static abstract class BaseNoteTask extends AsyncTask<Note, Void, Note> {
        @NonNull
        private NotesRepository repository;

        BaseNoteTask(@NonNull NotesRepository repository) {
            this.repository = repository;
        }

        @Override
        public void onPostExecute(@Nullable Note note) {
            if (note != null) {
                repository.notes.put(note.getGuid(), note);
                repository.notifyOnSynchronized();
                new NotesRepository.MergeNotesTask(repository).execute();
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
            return super.repository.notesDao.addNote(notes[0]);
        }
    }

    static class UpdateNoteTask extends BaseNoteTask {
        UpdateNoteTask(@NonNull NotesRepository repository) {
            super(repository);
        }

        @Nullable
        @Override
        protected Note doInBackground(@NonNull Note... notes) {
            return super.repository.notesDao.updateNote(notes[0]);
        }
    }

    static class DeleteNoteTask extends BaseNoteTask {
        DeleteNoteTask(@NonNull NotesRepository repository) {
            super(repository);
        }

        @Nullable
        @Override
        protected Note doInBackground(@NonNull Note... notes) {
            return super.repository.notesDao.deleteNote(notes[0]);
        }
    }
}
