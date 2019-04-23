package com.example.notes.domain;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.Objects;

/**
 * Представляет собой основные поля заметки
 */
public class Note implements Cloneable {
    @Nullable
    @SerializedName("guid")
    private String guid;
    @Nullable
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

    public Note() {
        this.lastUpdate = new Date();
    }

    public Note(@Nullable String guid, @NonNull String name, @Nullable String description) {
        this.guid = guid;
        this.name = name;
        this.description = description;
        this.lastUpdate = new Date();
    }

    @Nullable
    public String getGuid() {
        return guid;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @NonNull
    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setGuid(@Nullable String guid) {
        this.guid = guid;
    }

    public void setName(@NonNull String name) {
        this.name = name;
        this.lastUpdate = new Date();
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
        this.lastUpdate = new Date();
    }

    public void setLastUpdate(@NonNull Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void delete() {
        deleted = true;
        lastUpdate = new Date();
    }

    @NonNull
    @Override
    public Note clone() throws CloneNotSupportedException {
        return (Note) super.clone();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((guid == null) ? 0 : guid.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + lastUpdate.hashCode();
        result = prime * result + (deleted ? 1231 : 1237);
        return result;

    }

    @Override
    public boolean equals(@NonNull Object other) {
        if (this == other)
            return true;
        if (!(other instanceof Note))
            return false;
        Note otherNote = (Note) other;
        return (this.getGuid() != null && this.getGuid().equals(otherNote.getGuid())) &&
                (this.getName() != null && this.getName().equals(otherNote.getName())) &&
                (this.getDescription() != null && this.getDescription().equals(otherNote.getDescription())) &&
                this.getLastUpdate().equals(otherNote.getLastUpdate()) &&
                this.isDeleted() == otherNote.isDeleted();
    }
}
