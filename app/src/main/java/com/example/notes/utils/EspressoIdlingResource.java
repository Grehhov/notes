package com.example.notes.utils;

import android.support.annotation.NonNull;
import android.support.test.espresso.IdlingResource;

public class EspressoIdlingResource {
    @NonNull
    private static final String RESOURCE = "IDLINGRESOURCE";

    @NonNull
    private static ProgressIdlingResource countingIdlingResource = new ProgressIdlingResource(RESOURCE);

    public static void start() {
        countingIdlingResource.start();
    }

    public static void end() {
        countingIdlingResource.end();
    }

    @NonNull
    public static IdlingResource getIdlingResource() {
        return countingIdlingResource;
    }
}
