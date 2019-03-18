package com.example.notes;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Представляет собой основные поля заметки
 */
public class Note implements Parcelable {
    @NonNull
    private String name;
    @Nullable
    private String description;

    public Note(@NonNull String name, @Nullable String description) {
        this.name = name;
        this.description = description;
    }

    public Note(@NonNull Parcel in) {
        String[] data = new String[2];
        in.readStringArray(data);
        name = data[0];
        description = data[1];
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeStringArray(new String[] { name, description });
    }

    @NonNull
    public static final Parcelable.Creator<Note> CREATOR = new Parcelable.Creator<Note>() {

        @NonNull
        @Override
        public Note createFromParcel(@NonNull Parcel source) {
            return new Note(source);
        }

        @NonNull
        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };
}
