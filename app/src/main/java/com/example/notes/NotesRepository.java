package com.example.notes;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Содержит операции над хранилищем заметок
 */
public class NotesRepository {

    /**
     * Обрабатывает процесс обновления списка заметок
     */
    interface NotesRefreshListener {
        void onStartRefresh();
        void onCompleteRefresh(@NonNull List<Note> notes);
        void onError();
    }

    private interface NotesApi {
        @POST("/notes/sync")
        Call<NotesResponseBody> syncNotes(@NonNull @Body NotesRequestBody notesRequestBody);
    }

    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static final String USER_NAME = "USER_NAME";
    private static final String TAG = NotesRepository.class.getSimpleName();
    private long version = 0L;
    private int lastId = -1;
    @NonNull
    private List<Note> notes = new ArrayList<>();
    @Nullable
    private volatile static NotesRepository instance;
    @NonNull
    private HashSet<NotesRefreshListener> notesRefreshListeners = new HashSet<>();
    private NotesApi notesApi;

    @NonNull
    static NotesRepository getInstance() {
        if (instance == null) {
            synchronized (NotesRepository.class) {
                if (instance == null) {
                    instance = new NotesRepository();
                }
            }
        }
        return instance;
    }

    private NotesRepository() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateLongFormatTypeAdapter())
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        notesApi = retrofit.create(NotesApi.class);
    }

    void addNotesRefreshListener(@NonNull NotesRefreshListener listener) {
        notesRefreshListeners.add(listener);
    }

    void removeNotesRefreshListener(@NonNull NotesRefreshListener listener) {
        notesRefreshListeners.remove(listener);
    }

    private void notifyOnStartRefresh() {
        List<NotesRefreshListener> list = new ArrayList<>(notesRefreshListeners);
        for (NotesRefreshListener listener : list) {
            listener.onStartRefresh();
        }
    }

    private void notifyOnCompleteRefresh() {
        List<NotesRefreshListener> list = new ArrayList<>(notesRefreshListeners);
        for (NotesRefreshListener listener : list) {
            listener.onCompleteRefresh(notes);
        }
    }

    private void notifyOnError() {
        List<NotesRefreshListener> list = new ArrayList<>(notesRefreshListeners);
        for (NotesRefreshListener listener : list) {
            listener.onError();
        }
    }

    int getUniqueId() {
        return lastId + 1;
    }

    @NonNull
    List<Note> getNotes() {
        return notes;
    }

    @NonNull
    Note getNote(int id) {
        return notes.get(id);
    }

    void loadNotes() {
        NotesRequestBody body = new NotesRequestBody(version, USER_NAME, notes);
        new SyncNotes(this).execute(body);
    }

    void updateNote(@NonNull Note note) {
        syncNote(note);
    }

    void addNote(@NonNull Note note) {
        lastId++;
        syncNote(note);
    }

    void deleteNote(@NonNull Note note) {
        note.delete();
        syncNote(note);
    }

    private void syncNote(@NonNull Note note) {
        List<Note> notes = new ArrayList<>();
        notes.add(note);
        NotesRequestBody body = new NotesRequestBody(version, USER_NAME, notes);
        new SyncNotes(this).execute(body);
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

    private static class SyncNotes extends AsyncTask<NotesRequestBody, Void, NotesResponseBody> {
        @NonNull
        private NotesRepository notesRepository;

        SyncNotes(@NonNull NotesRepository notesRepository) {
            this.notesRepository = notesRepository;
        }

        @Override
        protected void onPreExecute() {
            notesRepository.notifyOnStartRefresh();
        }

        @Nullable
        @Override
        protected NotesResponseBody doInBackground(@NonNull NotesRequestBody... notesRequestBody)  {
            Response<NotesResponseBody> response = null;
            try {
                response = notesRepository.notesApi.syncNotes(notesRequestBody[0]).execute();
            } catch (IOException e) {
                Log.e(TAG, "Error accessing server", e);
            }
            return response == null ? null : response.body();
        }

        @Override
        protected void onPostExecute(@Nullable NotesResponseBody body) {
            if (body != null) {
                if (notesRepository.version + 1 == body.version) {
                    Note note = body.notes.get(body.notes.size() - 1);
                    if (note.getId() == notesRepository.notes.size()) {
                        notesRepository.notes.add(note);
                    } else {
                        notesRepository.notes.set(note.getId(), note);
                    }
                } else {
                    notesRepository.notes = body.notes;
                    notesRepository.lastId = body.notes.size() - 1;
                }
                notesRepository.version = body.version;
                notesRepository.notifyOnCompleteRefresh();
            } else {
                notesRepository.notifyOnError();
            }
        }
    }
}
