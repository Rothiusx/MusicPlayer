package com.example.rothi.musicplayer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UsersDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "music.db";

    private static final String USERS_TABLE_NAME = "users";
    private static final String USERS_COLUMN_ID = "id";
    private static final String USERS_COLUMN_EMAIL = "email";
    private static final String USERS_COLUMN_LOGIN = "login";
    private static final String USERS_COLUMN_PASSWORD = "password";
    private static final String USERS_COLUMN_FORENAME = "forename";
    private static final String USERS_COLUMN_SURNAME = "surname";

    public UsersDatabase(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String table = "CREATE TABLE users " +
                "(id INTEGER PRIMARY KEY, email TEXT, login TEXT, password TEXT, forename TEXT, surname TEXT)";
        db.execSQL(table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    public void deleteTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(USERS_TABLE_NAME, "1", null);
        db.close();
    }
}
