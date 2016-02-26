package com.example.newonnetflix.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.newonnetflix.data.MoviesProvider.Tables;

public class MoviesDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "newonnetflix.db";
    private static final int DATABASE_VERSION = 1;

    public MoviesDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.ITEMS + " ("
                + MoviesContract.ItemsColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MoviesContract.ItemsColumns.SERVER_ID + " TEXT,"
                + MoviesContract.ItemsColumns.TITLE + " TEXT NOT NULL,"
                + MoviesContract.ItemsColumns.AUTHOR + " TEXT NOT NULL,"
                + MoviesContract.ItemsColumns.BODY + " TEXT NOT NULL,"
                + MoviesContract.ItemsColumns.THUMB_URL + " TEXT NOT NULL,"
                + MoviesContract.ItemsColumns.PHOTO_URL + " TEXT NOT NULL,"
                + MoviesContract.ItemsColumns.ASPECT_RATIO + " REAL NOT NULL DEFAULT 1.5,"
                + MoviesContract.ItemsColumns.PUBLISHED_DATE + " TEXT NOT NULL"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Tables.ITEMS);
        onCreate(db);
    }
}
