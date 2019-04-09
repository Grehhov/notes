package com.example.notes;

import android.app.Application;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Содержит операции над хранилищем заметок
 */
class NotesRepository {

    /**
     * Обрабатывает процесс обновления списка заметок
     */
    interface NotesSynchronizedListener {
        void onSynchronized();
        void onError();
    }

    private static final String TAG = "NotesRepository";
    @NonNull
    private HashMap<String, Note> notes = new HashMap<>();
    @Nullable
    private volatile static NotesRepository instance;
    @NonNull
    private HashSet<NotesSynchronizedListener> notesSynchronizedListeners = new HashSet<>();
    private NotesDataSource notesDataSource;

    @NonNull
    static NotesRepository getInstance(Application application) {
        if (instance == null) {
            synchronized (NotesRepository.class) {
                if (instance == null) {
                    instance = new NotesRepository(application);
                }
            }
        }
        return instance;
    }

    private NotesRepository(Application application) {
        this.notesDataSource = new NotesDataSource(application);
    }

    void addNotesSynchronizedListener(@NonNull NotesSynchronizedListener listener) {
        notesSynchronizedListeners.add(listener);
    }

    void removeNotesSynchronizedListener(@NonNull NotesSynchronizedListener listener) {
        notesSynchronizedListeners.remove(listener);
    }

    private void notifyOnSynchronized() {
        List<NotesSynchronizedListener> list = new ArrayList<>(notesSynchronizedListeners);
        for (NotesSynchronizedListener listener : list) {
            listener.onSynchronized();
        }
    }

    private void notifyOnError() {
        List<NotesSynchronizedListener> list = new ArrayList<>(notesSynchronizedListeners);
        for (NotesSynchronizedListener listener : list) {
            listener.onError();
        }
    }

    @NonNull
    List<Note> getNotes() {
        return new ArrayList<>(notes.values());
    }

    @Nullable
    Note getNote(@NonNull String guid) {
        return notes.get(guid);
    }

    void loadNotes() {
        new GetAllTask( this).execute();
    }

    void updateNote(@NonNull Note note) {
        new UpdateNoteTask(this).execute(note);
    }

    void addNote(@NonNull Note note) {
        new AddNoteTask(this).execute(note);
    }

    void deleteNote(@NonNull Note note) {
        new DeleteNoteTask(this).execute(note);
    }

    private static class GetAllTask extends AsyncTask<Void, Void, List<Note>> {
        @NonNull
        private NotesRepository notesRepository;

        GetAllTask(@NonNull NotesRepository notesRepository) {
            this.notesRepository = notesRepository;
        }

        @Nullable
        @Override
        protected List<Note> doInBackground(Void... voids) {
            return notesRepository.notesDataSource.getAllNotes();
        }

        @Override
        public void onPostExecute(@Nullable List<Note> notes) {
            if (notes != null) {
                notesRepository.notes = new HashMap<>();
                for (Note note : notes) {
                    notesRepository.notes.put(note.getGuid(), note);
                }
                notesRepository.notifyOnSynchronized();
            } else {
                notesRepository.notifyOnError();
            }
        }
    }

    private static class AddNoteTask extends AsyncTask<Note, Void, Note> {
        @NonNull
        private NotesRepository notesRepository;

        AddNoteTask(@NonNull NotesRepository notesRepository) {
            this.notesRepository = notesRepository;
        }

        @Nullable
        @Override
        protected Note doInBackground(@NonNull Note... notes) {
            return notesRepository.notesDataSource.addNote(notes[0]);
        }

        @Override
        public void onPostExecute(@Nullable Note note) {
            if (note != null) {
                notesRepository.notes.put(note.getGuid(), note);
                notesRepository.notifyOnSynchronized();
            } else {
                notesRepository.notifyOnError();
            }
        }
    }

    private static class UpdateNoteTask extends AsyncTask<Note, Void, Note> {
        @NonNull
        private NotesRepository notesRepository;

        UpdateNoteTask(@NonNull NotesRepository notesRepository) {
            this.notesRepository = notesRepository;
        }

        @Nullable
        @Override
        protected Note doInBackground(@NonNull Note... notes) {
            return notesRepository.notesDataSource.updateNote(notes[0]);
        }

        @Override
        public void onPostExecute(@Nullable Note note) {
            if (note != null) {
                notesRepository.notes.put(note.getGuid(), note);
                notesRepository.notifyOnSynchronized();
            } else {
                notesRepository.notifyOnError();
            }
        }
    }

    private static class DeleteNoteTask extends AsyncTask<Note, Void, String> {
        @NonNull
        private NotesRepository notesRepository;

        DeleteNoteTask(@NonNull NotesRepository notesRepository) {
            this.notesRepository = notesRepository;
        }

        @Nullable
        @Override
        protected String doInBackground(@NonNull Note... notes) {
            int count = notesRepository.notesDataSource.deleteNote(notes[0]);

            return count == 0 ? null : notes[0].getGuid();
        }

        @Override
        public void onPostExecute(@Nullable String guid) {
            if (guid != null) {
                notesRepository.notes.remove(guid);
                notesRepository.notifyOnSynchronized();
            } else {
                notesRepository.notifyOnError();
            }
        }
    }
}
