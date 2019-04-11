package com.example.notes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Предоставляет методы взаимодействия с базой данных SQLite
 */
class NotesDao {
    private SQLiteDatabase database;
    @NonNull
    private NotesSqliteHelper dbHelper;
    @NonNull
    private String[] allColumns = {
            NotesSqliteHelper.COLUMN_GUID,
            NotesSqliteHelper.COLUMN_NAME,
            NotesSqliteHelper.COLUMN_DESCRIPTION,
            NotesSqliteHelper.COLUMN_LAST_UPDATE,
            NotesSqliteHelper.COLUMN_DELETED
    };

    NotesDao(@NonNull Context context) {
        dbHelper = new NotesSqliteHelper(context);
    }

    private void open() {
        database = dbHelper.getWritableDatabase();
    }

    private void close() {
        database.close();
    }

    @NonNull
    @WorkerThread
    List<Note> getAllNotes() {
        open();
        List<Note> notes = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(
                    NotesSqliteHelper.TABLE_NOTES,
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
                    if (cursor.getInt(4) == 1) {
                        note.delete();
                    }
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
    @WorkerThread
    private Note getNote(String guid) {
        open();
        Note note = null;
        String selection = NotesSqliteHelper.COLUMN_GUID + "=?";
        String[] selectionArgs = {guid};
        Cursor cursor = null;
        try {
            cursor = database.query(
                    NotesSqliteHelper.TABLE_NOTES,
                    allColumns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null);
            if (cursor.moveToFirst()) {
                note = new Note(cursor.getString(0), cursor.getString(1), cursor.getString(2));
                note.setLastUpdate(new Date(cursor.getLong(3)));
                if (cursor.getInt(4) == 1) {
                    note.delete();
                }
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
    @WorkerThread
    Note addNote(@NonNull Note note) {
        open();
        ContentValues values = new ContentValues();
        values.put(NotesSqliteHelper.COLUMN_GUID, note.getGuid());
        values.put(NotesSqliteHelper.COLUMN_NAME, note.getName());
        values.put(NotesSqliteHelper.COLUMN_DESCRIPTION, note.getDescription());
        values.put(NotesSqliteHelper.COLUMN_LAST_UPDATE, note.getLastUpdate().getTime());
        values.put(NotesSqliteHelper.COLUMN_DELETED, note.isDeleted() ? 1 : 0);
        try {
            database.insert(NotesSqliteHelper.TABLE_NOTES, null, values);
            return getNote(note.getGuid());
        } finally {
            close();
        }
    }

    @Nullable
    @WorkerThread
    Note updateNote(@NonNull Note note) {
        open();
        ContentValues values = new ContentValues();
        values.put(NotesSqliteHelper.COLUMN_NAME, note.getName());
        values.put(NotesSqliteHelper.COLUMN_DESCRIPTION, note.getDescription());
        values.put(NotesSqliteHelper.COLUMN_LAST_UPDATE, note.getLastUpdate().getTime());
        values.put(NotesSqliteHelper.COLUMN_DELETED, note.isDeleted() ? 1 : 0);
        String selection = NotesSqliteHelper.COLUMN_GUID + "=?";
        String[] whereArgs = {note.getGuid()};
        try {
            database.update(NotesSqliteHelper.TABLE_NOTES, values, selection, whereArgs);
            return getNote(note.getGuid());
        } finally {
            close();
        }
    }

    @Nullable
    @WorkerThread
    Note deleteNote(@NonNull Note note) {
        open();
        ContentValues values = new ContentValues();
        values.put(NotesSqliteHelper.COLUMN_NAME, note.getName());
        values.put(NotesSqliteHelper.COLUMN_DESCRIPTION, note.getDescription());
        values.put(NotesSqliteHelper.COLUMN_LAST_UPDATE, note.getLastUpdate().getTime());
        values.put(NotesSqliteHelper.COLUMN_DELETED, 1);
        String selection = NotesSqliteHelper.COLUMN_GUID + "=?";
        String[] whereArgs = {note.getGuid()};
        try {
            database.update(NotesSqliteHelper.TABLE_NOTES, values, selection, whereArgs);
            return getNote(note.getGuid());
        } finally {
            close();
        }
    }

    @WorkerThread
    void syncNotes(@NonNull List<Note> notes) {
        open();
        database.beginTransaction();
        try {
            for (Note note : notes) {
                ContentValues values = new ContentValues();
                values.put(NotesSqliteHelper.COLUMN_GUID, note.getGuid());
                values.put(NotesSqliteHelper.COLUMN_NAME, note.getName());
                values.put(NotesSqliteHelper.COLUMN_DESCRIPTION, note.getDescription());
                values.put(NotesSqliteHelper.COLUMN_LAST_UPDATE, note.getLastUpdate().getTime());
                values.put(NotesSqliteHelper.COLUMN_DELETED, note.isDeleted() ? 1 : 0);
                database.insertWithOnConflict(
                        NotesSqliteHelper.TABLE_NOTES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
            close();
        }
    }
}
