package com.example.notes.di;

import android.support.annotation.NonNull;

import com.example.notes.data.LocalRepositoryImpl;
import com.example.notes.data.NotesDaoImpl;
import com.example.notes.domain.DateLongFormatTypeAdapter;
import com.example.notes.domain.NotesInteractor;
import com.example.notes.data.NotesApi;
import com.example.notes.data.RemoteRepositoryImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
class RepositoryModule {
    private static final String BASE_URL = "http://10.0.2.2:8080/";

    @Provides
    @NonNull
    @Singleton
    NotesInteractor provideNotesInteractor(@NonNull LocalRepositoryImpl localRepositoryImpl,
                                      @NonNull RemoteRepositoryImpl remoteRepositoryImpl) {
        return new NotesInteractor(localRepositoryImpl, remoteRepositoryImpl);
    }

    @Provides
    @NonNull
    @Singleton
    LocalRepositoryImpl provideLocalRepository(@NonNull NotesDaoImpl notesDaoImpl) {
        return new LocalRepositoryImpl(notesDaoImpl);
    }

    @Provides
    @NonNull
    @Singleton
    RemoteRepositoryImpl provideRemoteRepository(@NonNull NotesApi notesApi) {
        return new RemoteRepositoryImpl(notesApi);
    }


    @Provides
    @NonNull
    NotesApi provideNotesApi() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateLongFormatTypeAdapter())
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        return retrofit.create(NotesApi.class);
    }
}
