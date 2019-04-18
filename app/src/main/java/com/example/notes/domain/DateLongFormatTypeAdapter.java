package com.example.notes.domain;


import android.support.annotation.NonNull;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Date;

public class DateLongFormatTypeAdapter extends TypeAdapter<Date> {
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
