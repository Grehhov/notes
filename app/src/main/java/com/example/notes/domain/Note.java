package com.example.notes.domain;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

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
}
