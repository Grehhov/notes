package com.example.notes;

import android.app.Application;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

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

    private interface NotesApi {
        @POST("/notes/sync")
        Call<NotesResponseBody> syncNotes(@NonNull @Body NotesRequestBody notesRequestBody);
    }

    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static final String USER_NAME = "USER_NAME_V7";
    private static final String TAG = "NotesRepository";
    @NonNull
    HashMap<String, Note> notes = new HashMap<>();
    @Nullable
    private volatile static NotesRepository instance;
    @NonNull
    private HashSet<NotesSynchronizedListener> notesSynchronizedListeners = new HashSet<>();
    private NotesApi notesApi;
    NotesDao notesDao;

    @NonNull
    static NotesRepository getInstance(@NonNull Application application) {
        if (instance == null) {
            synchronized (NotesRepository.class) {
                if (instance == null) {
                    instance = new NotesRepository(application);
                }
            }
        }
        return instance;
    }

    private NotesRepository(@NonNull Application application) {
        notesApi = createNotesApi();
        notesDao = new NotesDao(application);
    }

    private NotesApi createNotesApi() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateLongFormatTypeAdapter())
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit.create(NotesApi.class);
    }

    void addNotesSynchronizedListener(@NonNull NotesSynchronizedListener listener) {
        notesSynchronizedListeners.add(listener);
    }

    void removeNotesSynchronizedListener(@NonNull NotesSynchronizedListener listener) {
        notesSynchronizedListeners.remove(listener);
    }

    void notifyOnSynchronized() {
        List<NotesSynchronizedListener> list = new ArrayList<>(notesSynchronizedListeners);
        for (NotesSynchronizedListener listener : list) {
            listener.onSynchronized();
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
        new AsyncSqliteHelper.GetAllTask(this).execute();
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

    @Nullable
    @WorkerThread
    private Response<NotesResponseBody> syncNotes(@NonNull List<Note> notes) {
        Response<NotesResponseBody> response = null;
        try {
            response = notesApi
                    .syncNotes(new NotesRequestBody(0, USER_NAME, notes))
                    .execute();
        } catch (IOException e) {
            Log.e(TAG, "Error accessing server", e);
        }

        return response;
    }

    private static class DateLongFormatTypeAdapter extends TypeAdapter<Date> {
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

        @Nullable
        @Override
        protected HashMap<String, Note> doInBackground(Void... params) {
            HashMap<String, Note> mergedNotes = notesRepository.notes;

            Response<NotesResponseBody> response = notesRepository.syncNotes(new ArrayList<Note>());
            if (response != null && response.body() != null) {
                List<Note> remoteNotes = response.body().notes;
                for (Note remoteNote : remoteNotes) {
                    if (isNeedUpdate(remoteNote, mergedNotes)) {
                        mergedNotes.put(remoteNote.getGuid(), remoteNote);
                    }
                }
                List<Note> mergedNoteList = new ArrayList<>(mergedNotes.values());
                notesRepository.notesDao.syncNotes(mergedNoteList);
                notesRepository.syncNotes(mergedNoteList);
            }

            return mergedNotes;
        }

        @Override
        protected void onPostExecute(@Nullable HashMap<String, Note> mergedNotes) {
            if (mergedNotes != null) {
                notesRepository.notes = mergedNotes;
                notesRepository.notifyOnSynchronized();
            } else {
                notesRepository.notifyOnError();
            }
        }
    }
}
