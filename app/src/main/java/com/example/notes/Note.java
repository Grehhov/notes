package com.example.notes;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

/**
 * Представляет собой основные поля заметки
 */
public class Note implements Cloneable {
    @Nullable
    private String guid;
    @Nullable
    private String name;
    @Nullable
    private String description;
    @NonNull
    private Date lastUpdate;
    private boolean deleted;

    Note() {
        this.lastUpdate = new Date();
    }

    Note(@Nullable String guid, @NonNull String name, @Nullable String description) {
        this.guid = guid;
        this.name = name;
        this.description = description;
        this.lastUpdate = new Date();
    }

    @Nullable
    String getGuid() {
        return guid;
    }

    @Nullable
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

    void setGuid(@Nullable String guid) {
        this.guid = guid;
    }

    void setName(@NonNull String name) {
        this.name = name;
        this.lastUpdate = new Date();
    }

    void setDescription(@Nullable String description) {
        this.description = description;
        this.lastUpdate = new Date();
    }

    void setLastUpdate(@NonNull Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    boolean isDeleted() {
        return deleted;
    }

    void delete() {
        deleted = true;
        lastUpdate = new Date();
    }

    @NonNull
    @Override
    protected Note clone() throws CloneNotSupportedException {
        return (Note) super.clone();
    }
}
