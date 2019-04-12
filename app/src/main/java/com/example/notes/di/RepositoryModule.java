package com.example.notes.di;

import android.support.annotation.NonNull;

import com.example.notes.NotesDao;
import com.example.notes.NotesRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
class RepositoryModule {
    private static final String BASE_URL = "http://10.0.2.2:8080/";

    @Provides
    @NonNull
    @Singleton
    NotesRepository provideRepository(@NonNull NotesRepository.NotesApi notesApi, @NonNull NotesDao notesDao) {
        return new NotesRepository(notesApi, notesDao);
    }

    @Provides
    @NonNull
    NotesRepository.NotesApi provideNotesApi() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new NotesRepository.DateLongFormatTypeAdapter())
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit.create(NotesRepository.NotesApi.class);
    }
}
