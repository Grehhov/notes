package com.example.notes;

/**
 * Представляет собой основные поля заметки
 */

public class Note {
    private String name;
    private String description;

    public Note(String name, String description) {
        this.name = name;
        this.description = description;
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
}
