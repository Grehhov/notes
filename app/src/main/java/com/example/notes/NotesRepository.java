package com.example.notes;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Содержит операции над хранилищем заметок
 */
public class NotesRepository {

    /**
     * Обрабатывает процесс обновления списка заметок
     */
    interface NotesSynchronizedListener {
        void onSynchronized();
        void onSynchronizedWithNetwork();
        void onError();
    }

    public interface NotesApi {
        @POST("/notes/sync")
        Call<NotesResponseBody> syncNotes(@NonNull @Body NotesRequestBody notesRequestBody);
    }

    private static final String USER_NAME = "USER_NAME_V7";
    private static final String TAG = "NotesRepository";
    @NonNull
    private HashMap<String, Note> notes = new HashMap<>();
    @NonNull
    private HashSet<NotesSynchronizedListener> notesSynchronizedListeners = new HashSet<>();
    @NonNull
    private final NotesApi notesApi;
    @NonNull
    private final NotesDao notesDao;
    @Nullable
    private MergeNotesTask lastMergeNotesTask;

    public NotesRepository(@NonNull NotesApi notesApi, @NonNull NotesDao notesDao) {
        this.notesApi = notesApi;
        this.notesDao = notesDao;
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
        if (lastMergeNotesTask != null) {
            lastMergeNotesTask.cancel(true);
        }
        lastMergeNotesTask = new NotesRepository.MergeNotesTask(this);
        lastMergeNotesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void notifyOnSynchronizedWithNetwork() {
        List<NotesSynchronizedListener> list = new ArrayList<>(notesSynchronizedListeners);
        for (NotesSynchronizedListener listener : list) {
            listener.onSynchronizedWithNetwork();
        }
    }

    void notifyOnError() {
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
        new AsyncSqliteHelper.LoadNotesTask(this).execute();
    }

    void updateNote(@NonNull Note note) {
        new AsyncSqliteHelper.UpdateNoteTask(this).execute(note);
    }

    void addNote(@NonNull Note note) {
        note.setGuid(UUID.randomUUID().toString());
        new AsyncSqliteHelper.AddNoteTask(this).execute(note);
    }

    void deleteNote(@NonNull Note note) {
        new AsyncSqliteHelper.DeleteNoteTask(this).execute(note);
    }

    @NonNull
    List<Note> getAllNotesDb() {
        return notesDao.getAllNotes();
    }

    @Nullable
    Note addNoteDb(@NonNull Note note) {
        return notesDao.addNote(note);
    }

    @Nullable
    Note updateNoteDb(@NonNull Note note) {
        return notesDao.updateNote(note);
    }

    @Nullable
    Note deleteNoteDb(@NonNull Note note) {
        return notesDao.deleteNote(note);
    }

    void syncNotesDb(@NonNull List<Note> notes) {
        notesDao.syncNotes(notes);
    }

    void updateNotes(@NonNull List<Note> newNotes) {
        notes = new HashMap<>();
        for (Note note : newNotes) {
            notes.put(note.getGuid(), note);
        }
        notifyOnSynchronized();
    }

    void updateNotes(@NonNull Note note) {
        notes.put(note.getGuid(), note);
        notifyOnSynchronized();
    }

    public static class DateLongFormatTypeAdapter extends TypeAdapter<Date> {
        @Override
        public void write(@NonNull JsonWriter out, @NonNull Date value) throws IOException {
            out.value(String.valueOf(value.getTime()));
        }

        @NonNull
        @Override
        public Date read(@NonNull JsonReader in) throws IOException {
            return new Date(in.nextLong());
        }
    }

    private static class NotesRequestBody {
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

    private static class NotesResponseBody {
        public long version;
        @NonNull
        public List<Note> notes;

        NotesResponseBody(long version, @NonNull List<Note> notes) {
            this.version = version;
            this.notes = notes;
        }
    }

    static class MergeNotesTask extends AsyncTask<Void, Void, HashMap<String, Note>> {
        @NonNull
        private NotesRepository notesRepository;

        MergeNotesTask(@NonNull NotesRepository notesRepository) {
            this.notesRepository = notesRepository;
        }

        private boolean isNeedUpdate(@NonNull Note remoteNote, @NonNull HashMap<String, Note> mergedNotes) {
            String guid = remoteNote.getGuid();
            boolean keyIsContains = mergedNotes.containsKey(guid);
            boolean isFreshest = false;
            if (keyIsContains) {
                long remoteLastUpdate = remoteNote.getLastUpdate().getTime();
                Note localNote = mergedNotes.get(guid);
                if (localNote != null) {
                    long localLastUpdate = localNote.getLastUpdate().getTime();
                    isFreshest = remoteLastUpdate > localLastUpdate;
                }
            }
            return !keyIsContains || isFreshest;
        }

        @WorkerThread
        private Response<NotesResponseBody> syncRemoteNotes(@NonNull List<Note> notes) {
            Response<NotesResponseBody> response = null;
            try {
                response = notesRepository
                        .notesApi
                        .syncNotes(new NotesRequestBody(0, USER_NAME, notes))
                        .execute();
            } catch (InterruptedIOException e) {
                if (!isCancelled()) {
                    Log.e(TAG, "Thread interrupted", e);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error accessing server", e);
            }

            return response;
        }

        @Nullable
        @Override
        protected HashMap<String, Note> doInBackground(Void... params) {
            Response<NotesResponseBody> response = null;
            HashMap<String, Note> mergedNotes = notesRepository.notes;
            try {
                response = syncRemoteNotes(new ArrayList<Note>());
                if (response != null && response.body() != null) {
                    List<Note> remoteNotes = response.body().notes;
                    for (Note remoteNote : remoteNotes) {
                        if (isCancelled()) {
                            throw new InterruptedIOException();
                        }
                        if (isNeedUpdate(remoteNote, mergedNotes)) {
                            mergedNotes.put(remoteNote.getGuid(), remoteNote);
                        }
                    }
                    List<Note> mergedNoteList = new ArrayList<>(mergedNotes.values());
                    response = syncRemoteNotes(mergedNoteList);
                    if (response != null) {
                        notesRepository.syncNotesDb(mergedNoteList);
                    }
                }
            } catch (InterruptedIOException e) {
                if (!isCancelled()) {
                    Log.e(TAG, "Thread interrupted", e);
                }
            }
            return response == null ? null : mergedNotes;
        }

        @Override
        protected void onPostExecute(@Nullable HashMap<String, Note> mergedNotes) {
            if (mergedNotes != null) {
                notesRepository.notes = mergedNotes;
                notesRepository.notifyOnSynchronizedWithNetwork();
            }
        }
    }
}
