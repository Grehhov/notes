package com.example.notes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class NotesSQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;

    static final String TABLE_NOTES = "notes";

    static final String COLUMN_GUID = "guid";
    static final String COLUMN_NAME = "name";
    static final String COLUMN_DESCRIPTION = "description";
    static final String COLUMN_LAST_UPDATE = "last_update";
    static final String COLUMN_DELETED = "deleted";

    private static final String SQL_CREATE_TABLE_NOTES = "create table " + TABLE_NOTES + "(" +
            COLUMN_GUID + " text primary key," +
            COLUMN_NAME + " text," +
            COLUMN_DESCRIPTION + " text," +
            COLUMN_LAST_UPDATE + " integer," +
            COLUMN_DELETED + " integer)";

    private static final String SQL_DROP_TABLE_NOTES = "drop table if exists " + TABLE_NOTES;

    NotesSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_NOTES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP_TABLE_NOTES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}