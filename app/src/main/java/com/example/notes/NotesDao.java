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

class NotesDao {
    private SQLiteDatabase database;
    @NonNull
    private NotesSQLiteHelper dbHelper;
    private String[] allColumns = {
            NotesSQLiteHelper.COLUMN_GUID,
            NotesSQLiteHelper.COLUMN_NAME,
            NotesSQLiteHelper.COLUMN_DESCRIPTION,
            NotesSQLiteHelper.COLUMN_LAST_UPDATE,
            NotesSQLiteHelper.COLUMN_DELETED
    };

    NotesDao(@NonNull Context context) {
        dbHelper = new NotesSQLiteHelper(context);
    }

    void open() {
        database = dbHelper.getWritableDatabase();
    }

    void close() {
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
        String selection = NotesSQLiteHelper.COLUMN_GUID + "=?";
        String[] selectionArgs = {guid};
        Cursor cursor = null;
        try {
            cursor = database.query(
                    NotesSQLiteHelper.TABLE_NOTES,
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
        values.put(NotesSQLiteHelper.COLUMN_GUID, note.getGuid());
        values.put(NotesSQLiteHelper.COLUMN_NAME, note.getName());
        values.put(NotesSQLiteHelper.COLUMN_DESCRIPTION, note.getDescription());
        values.put(NotesSQLiteHelper.COLUMN_LAST_UPDATE, note.getLastUpdate().getTime());
        values.put(NotesSQLiteHelper.COLUMN_DELETED, note.isDeleted() ? 1 : 0);
        try {
            database.insert(NotesSQLiteHelper.TABLE_NOTES, null, values);
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
        values.put(NotesSQLiteHelper.COLUMN_NAME, note.getName());
        values.put(NotesSQLiteHelper.COLUMN_DESCRIPTION, note.getDescription());
        values.put(NotesSQLiteHelper.COLUMN_LAST_UPDATE, note.getLastUpdate().getTime());
        values.put(NotesSQLiteHelper.COLUMN_DELETED, note.isDeleted() ? 1 : 0);
        String selection = NotesSQLiteHelper.COLUMN_GUID + "=?";
        String[] whereArgs = {note.getGuid()};
        try {
            database.update(NotesSQLiteHelper.TABLE_NOTES, values, selection, whereArgs);
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
        values.put(NotesSQLiteHelper.COLUMN_NAME, note.getName());
        values.put(NotesSQLiteHelper.COLUMN_DESCRIPTION, note.getDescription());
        values.put(NotesSQLiteHelper.COLUMN_LAST_UPDATE, note.getLastUpdate().getTime());
        values.put(NotesSQLiteHelper.COLUMN_DELETED, 1);
        String selection = NotesSQLiteHelper.COLUMN_GUID + "=?";
        String[] whereArgs = {note.getGuid()};
        try {
            database.update(NotesSQLiteHelper.TABLE_NOTES, values, selection, whereArgs);
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
                values.put(NotesSQLiteHelper.COLUMN_GUID, note.getGuid());
                values.put(NotesSQLiteHelper.COLUMN_NAME, note.getName());
                values.put(NotesSQLiteHelper.COLUMN_DESCRIPTION, note.getDescription());
                values.put(NotesSQLiteHelper.COLUMN_LAST_UPDATE, note.getLastUpdate().getTime());
                values.put(NotesSQLiteHelper.COLUMN_DELETED, note.isDeleted() ? 1 : 0);
                database.insertWithOnConflict(
                        NotesSQLiteHelper.TABLE_NOTES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
            close();
        }
    }
}
