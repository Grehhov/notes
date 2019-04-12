package com.example.notes.di;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ContextModule {
    @NonNull
    private Context context;

    public ContextModule(@NonNull Context context) {
        this.context = context;
    }

    @Provides
    @NonNull
    @Singleton
    Context provideContext() {
        return context;
    }
}
