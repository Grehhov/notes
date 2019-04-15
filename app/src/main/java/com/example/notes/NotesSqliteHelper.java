package com.example.notes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NotesSqliteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_TABLE_NOTES = "create table " + NotesDao.TABLE_NOTES + "(" +
            NotesDao.COLUMN_GUID + " text primary key," +
            NotesDao.COLUMN_NAME + " text," +
            NotesDao.COLUMN_DESCRIPTION + " text," +
            NotesDao.COLUMN_LAST_UPDATE + " integer," +
            NotesDao.COLUMN_DELETED + " integer)";

    private static final String SQL_DROP_TABLE_NOTES = "drop table if exists " + NotesDao.TABLE_NOTES;

    @Inject
    NotesSqliteHelper(@NonNull Context context) {
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