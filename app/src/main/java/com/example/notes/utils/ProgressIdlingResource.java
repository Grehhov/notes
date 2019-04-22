package com.example.notes.utils;

import android.support.annotation.NonNull;
import android.support.test.espresso.IdlingResource;


public class ProgressIdlingResource implements IdlingResource {
    @NonNull
    private final String resourceName;

    private boolean inProgress = false;

    private volatile ResourceCallback resourceCallback;

    ProgressIdlingResource(@NonNull String resourceName) {
        this.resourceName = resourceName;
    }

    @Override
    @NonNull
    public String getName() {
        return resourceName;
    }

    @Override
    public boolean isIdleNow() {
        return !inProgress;
    }

    @Override
    public void registerIdleTransitionCallback(@NonNull ResourceCallback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }

    void start() {
        inProgress = true;
    }

    void end() {
        inProgress = false;
        if (resourceCallback != null) {
            resourceCallback.onTransitionToIdle();
        }
    }
}