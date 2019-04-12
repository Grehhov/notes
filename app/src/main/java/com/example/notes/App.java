package com.example.notes;

import android.app.Application;

import com.example.notes.di.AppComponent;
import com.example.notes.di.ContextModule;
import com.example.notes.di.DaggerAppComponent;

public class App extends Application {
    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        appComponent = DaggerAppComponent
                .builder()
                .contextModule(new ContextModule(this))
                .build();
    }

    public AppComponent getComponent() {
        return appComponent;
    }
}
