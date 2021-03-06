package com.example.notes.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.example.notes.domain.Note;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Предоставляет методы взаимодействия с базой данных SQLite
 */
@Singleton
public class NotesDaoImpl implements NotesDao {
    static final String TABLE_NOTES = "notes";

    static final String COLUMN_GUID = "guid";
    static final String COLUMN_NAME = "name";
    static final String COLUMN_DESCRIPTION = "description";
    static final String COLUMN_LAST_UPDATE = "last_update";
    static final String COLUMN_DELETED = "deleted";

    private final SQLiteDatabase database;
    @NonNull
    private final String[] allColumns = {
            COLUMN_GUID,
            COLUMN_NAME,
            COLUMN_DESCRIPTION,
            COLUMN_LAST_UPDATE,
            COLUMN_DELETED
    };

    @Inject
    NotesDaoImpl(@NonNull NotesSqliteHelper dbHelper) {
        database = dbHelper.getWritableDatabase();
    }

    @NonNull
    @WorkerThread
    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(
                    TABLE_NOTES,
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
        }
        return notes;
    }

    @Nullable
    @WorkerThread
    public Note getNote(@NonNull String guid) {
        Note note = null;
        String selection = COLUMN_GUID + "=?";
        String[] selectionArgs = {guid};
        Cursor cursor = null;
        try {
            cursor = database.query(
                    TABLE_NOTES,
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
        }

        return note;
    }

    @Nullable
    @WorkerThread
    public Note addNote(@NonNull Note note) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_GUID, note.getGuid());
        values.put(COLUMN_NAME, note.getName());
        values.put(COLUMN_DESCRIPTION, note.getDescription());
        values.put(COLUMN_LAST_UPDATE, note.getLastUpdate().getTime());
        values.put(COLUMN_DELETED, note.isDeleted() ? 1 : 0);
        database.insert(TABLE_NOTES, null, values);
        return note.getGuid() == null ? null :getNote(note.getGuid());
    }

    @Nullable
    @WorkerThread
    public Note updateNote(@NonNull Note note) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, note.getName());
        values.put(COLUMN_DESCRIPTION, note.getDescription());
        values.put(COLUMN_LAST_UPDATE, note.getLastUpdate().getTime());
        values.put(COLUMN_DELETED, note.isDeleted() ? 1 : 0);
        String selection = COLUMN_GUID + "=?";
        String[] whereArgs = {note.getGuid()};
        database.update(TABLE_NOTES, values, selection, whereArgs);
        return note.getGuid() == null ? null :getNote(note.getGuid());
    }

    @Nullable
    @WorkerThread
    public Note deleteNote(@NonNull String guid) {
        Note note = getNote(guid);
        if (note != null) {
            note.delete();
            return updateNote(note);
        }
        return null;
    }

    @WorkerThread
    public void syncNotes(@NonNull List<Note> notes) {
        database.beginTransaction();
        try {
            for (Note note : notes) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_GUID, note.getGuid());
                values.put(COLUMN_NAME, note.getName());
                values.put(COLUMN_DESCRIPTION, note.getDescription());
                values.put(COLUMN_LAST_UPDATE, note.getLastUpdate().getTime());
                values.put(COLUMN_DELETED, note.isDeleted() ? 1 : 0);
                database.insertWithOnConflict(TABLE_NOTES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }
}
