package com.example.notes;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

/**
 * Представляет собой основные поля заметки
 */
public class Note implements Parcelable {
    @NonNull
    private String name;
    @Nullable
    private String description;
    @NonNull
    private Date addDate;
    @NonNull
    private Date lastUpdate;

    public Note(@NonNull String name, @Nullable String description) {
        this.name = name;
        this.description = description;
        this.addDate = new Date();
        this.lastUpdate = new Date();
    }

    public Note(@NonNull Parcel in) {
        name = in.readString();
        description = in.readString();
        addDate = new Date(in.readLong());
        lastUpdate = new Date(in.readLong());
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @NonNull
    public Date getAddDate() {
        return addDate;
    }

    @NonNull
    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setName(@NonNull String name) {
        this.name = name;
        this.lastUpdate = new Date();
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
        this.lastUpdate = new Date();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeLong(addDate.getTime());
        dest.writeLong(lastUpdate.getTime());
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
