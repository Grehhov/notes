package com.example.notes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Представляет собой основные поля заметки
 */

public class Note implements Parcelable {
    private String name;
    private String description;

    public Note(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Note(Parcel in) {
        String[] data = new String[2];
        in.readStringArray(data);
        name = data[0];
        description = data[1];
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] { name, description });
    }

    public static final Parcelable.Creator<Note> CREATOR = new Parcelable.Creator<Note>() {

        @Override
        public Note createFromParcel(Parcel source) {
            return new Note(source);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };
}
