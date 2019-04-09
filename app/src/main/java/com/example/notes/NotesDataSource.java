package com.example.notes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class NotesDataSource {
    private SQLiteDatabase database;
    @NonNull
    private NotesSQLiteHelper dbHelper;
    private String[] allColumns = {
            NotesSQLiteHelper.COLUMN_GUID,
            NotesSQLiteHelper.COLUMN_NAME,
            NotesSQLiteHelper.COLUMN_DESCRIPTION,
            NotesSQLiteHelper.COLUMN_LAST_UPDATE,
    };

    NotesDataSource(@NonNull Context context) {
        dbHelper = new NotesSQLiteHelper(context);
    }

    void open() {
        database = dbHelper.getWritableDatabase();
    }

    void close() {
        database.close();
    }

    @NonNull
    List<Note> getAllNotes() {
        open();
        List<Note> notes = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(
                    NotesSQLiteHelper.TABLE_NOTES,
                    allColumns,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            if (cursor.moveToFirst()) {
                do {
                    Note note = new Note(cursor.getString(0), cursor.getString(1), cursor.getString(2));
                    note.setLastUpdate(new Date(cursor.getLong(3)));
                    notes.add(note);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            close();
        }
        return notes;
    }

    @Nullable
    private Note getNote(String guid) {
        open();
        Note note = null;
        String selection = NotesSQLiteHelper.COLUMN_GUID + "=" + guid;
        Cursor cursor = null;
        try {
            cursor = database.query(
                    NotesSQLiteHelper.TABLE_NOTES,
                    allColumns,
                    selection,
                    null,
                    null,
                    null,
                    null);
            if (cursor.moveToFirst()) {
                note = new Note(cursor.getString(0), cursor.getString(1), cursor.getString(2));
                note.setLastUpdate(new Date(cursor.getLong(3)));
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            close();
        }

        return note;
    }

    @Nullable
    Note addNote(@NonNull Note note) {
        open();
        ContentValues values = new ContentValues();
        values.put(NotesSQLiteHelper.COLUMN_NAME, note.getName());
        values.put(NotesSQLiteHelper.COLUMN_DESCRIPTION, note.getDescription());
        values.put(NotesSQLiteHelper.COLUMN_LAST_UPDATE, note.getLastUpdate().getTime());
        try {
            long id = database.insert(NotesSQLiteHelper.TABLE_NOTES, null, values);

            return getNote(String.valueOf(id));
        } finally {
            close();
        }
    }

    @Nullable
    Note updateNote(@NonNull Note note) {
        open();
        ContentValues values = new ContentValues();
        values.put(NotesSQLiteHelper.COLUMN_NAME, note.getName());
        values.put(NotesSQLiteHelper.COLUMN_DESCRIPTION, note.getDescription());
        values.put(NotesSQLiteHelper.COLUMN_LAST_UPDATE, note.getLastUpdate().getTime());
        String selection = NotesSQLiteHelper.COLUMN_GUID + "=" + note.getGuid();
        try {
            database.update(NotesSQLiteHelper.TABLE_NOTES, values, selection, null);
            return getNote(note.getGuid());
        } finally {
            close();
        }
    }

    int deleteNote(@NonNull Note note) {
        open();
        String selection = NotesSQLiteHelper.COLUMN_GUID + "=" + note.getGuid();
        try {
            return database.delete(NotesSQLiteHelper.TABLE_NOTES, selection, null);
        } finally {
            close();
        }
    }
}
