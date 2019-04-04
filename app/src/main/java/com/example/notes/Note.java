package com.example.notes;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Представляет собой основные поля заметки
 */
public class Note implements Parcelable, Cloneable {
    @SerializedName("guid")
    private int id;
    @NonNull
    @SerializedName("title")
    private String name;
    @Nullable
    @SerializedName("content")
    private String description;
    @NonNull
    @SerializedName("date")
    private Date lastUpdate;
    @SerializedName("deleted")
    private boolean deleted;

    Note(int id, @NonNull String name, @Nullable String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.lastUpdate = new Date();
    }

    private Note(@NonNull Parcel in) {
        id = in.readInt();
        name = in.readString();
        description = in.readString();
        lastUpdate = new Date(in.readLong());
        deleted = in.readInt() == 1;
    }

    int getId() {
        return id;
    }

    @NonNull
    String getName() {
        return name;
    }

    @Nullable
    String getDescription() {
        return description;
    }

    @NonNull
    Date getLastUpdate() {
        return lastUpdate;
    }

    void setName(@NonNull String name) {
        this.name = name;
        this.lastUpdate = new Date();
    }

    void setDescription(@Nullable String description) {
        this.description = description;
        this.lastUpdate = new Date();
    }

    boolean isDeleted() {
        return deleted;
    }

    void delete() {
        deleted = true;
        lastUpdate = new Date();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeLong(lastUpdate.getTime());
        dest.writeInt(deleted ? 1 : 0);
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

    @NonNull
    @Override
    protected Note clone() throws CloneNotSupportedException {
        return (Note) super.clone();
    }
}
