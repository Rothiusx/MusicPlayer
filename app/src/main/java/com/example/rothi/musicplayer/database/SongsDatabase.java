package com.example.rothi.musicplayer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.rothi.musicplayer.media.Song;

import java.util.ArrayList;

public class SongsDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "music.db";

    private static final String SONGS_TABLE_NAME = "songs";
    private static final String SONGS_COLUMN_ID = "id";
    private static final String SONGS_COLUMN_TITLE = "title";
    private static final String SONGS_COLUMN_FAVORITE = "favorite";
    private static final String SONGS_COLUMN_ARTIST = "artist";
    private static final String SONGS_COLUMN_ALBUM = "album";

    public SongsDatabase(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String table = "CREATE TABLE songs " +
                "(id INTEGER PRIMARY KEY, title TEXT, artist TEXT, favorite INTEGER)";
        db.execSQL(table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        /*
        // If you need to add a column
        if (newVersion > oldVersion) {
                if (!columnExists()) {
                db.execSQL("ALTER TABLE table ADD COLUMN column INTEGER");
                }
        }
        */

        db.execSQL("DROP TABLE IF EXISTS songs");
        onCreate(db);
    }

    // Load Data
    public ArrayList<Song> loadSongList() {
        ArrayList<Song> songList = new ArrayList<>();
        //String result = "";
        String query = "SELECT * FROM " + SONGS_TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String title = cursor.getString(1);
            String artist = cursor.getString(2);
            int favorite = cursor.getInt(3);


            songList.add(new Song(id, title, artist, getBoolean(favorite)));
            /*
            result += String.valueOf(id) + " " + title + " - " + artist
                    + System.getProperty("line.separator");
                    */
        }

        cursor.close();
        db.close();

        return songList;
    }

    public void insertSong(Song song) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();

            values.put(SONGS_COLUMN_ID, song.getId());
            values.put(SONGS_COLUMN_TITLE, song.getTitle());
            values.put(SONGS_COLUMN_ARTIST, song.getArtist());
            values.put(SONGS_COLUMN_FAVORITE, getInteger(song.isFavorite()));

            db.insert(SONGS_TABLE_NAME, null, values);
            db.close();

        } catch (SQLiteException e) {
            Log.e("DB ERROR", "Error with adding new song to database: " +
                    e.getMessage(), e);
            e.getStackTrace();

        } finally {
            if (db.isOpen()) {
                db.close();
            }
        }
    }

    public Song findSong(int id) {
        String query = "SELECT * FROM " + SONGS_TABLE_NAME + " WHERE " +
                SONGS_COLUMN_ID + " = " + "'" + String.valueOf(id) + "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        Song song = new Song();

        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            song.setId(Integer.parseInt(cursor.getString(0)));
            song.setTitle(cursor.getString(1));
            song.setArtist(cursor.getString(2));
            song.setFavorite(getBoolean(cursor.getInt(3)));
            cursor.close();

        } else {
            song = null;
        }

        db.close();
        return song;
    }

    public Song findSong(String title, String artist) {
        String query = "SELECT * FROM " + SONGS_TABLE_NAME + " WHERE " +
                SONGS_COLUMN_TITLE + " = " + "'" + title + "'" + " AND " +
                SONGS_COLUMN_ARTIST + " = " + "'" + artist + "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        Song song = new Song();

        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            song.setId(Integer.parseInt(cursor.getString(0)));
            song.setTitle(cursor.getString(1));
            song.setArtist(cursor.getString(2));
            song.setFavorite(getBoolean(cursor.getInt(3)));
            cursor.close();

        } else {
            song = null;
        }

        db.close();
        return song;
    }

    public boolean deleteSong(int id) {
        boolean result = false;

        String query = "SELECT * FROM " + SONGS_TABLE_NAME + " WHERE " +
                SONGS_COLUMN_ID + " = " + "'" + String.valueOf(id) + "'";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        Song song = new Song();

        if (cursor.moveToFirst()) {
            song.setId(Integer.parseInt(cursor.getString(0)));
            db.delete(SONGS_TABLE_NAME, SONGS_COLUMN_ID + "=?",
                    new String[] {
                            String.valueOf(song.getId())
                    });

            cursor.close();
            result = true;
        }

        db.close();
        return result;
    }

    public boolean updateSong(int id, String title, String artist, boolean favorite) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues args = new ContentValues();

        args.put(SONGS_COLUMN_ID, id);
        args.put(SONGS_COLUMN_TITLE, title);
        args.put(SONGS_COLUMN_ARTIST, artist);
        args.put(SONGS_COLUMN_FAVORITE, getInteger(favorite));

        return db.update(SONGS_TABLE_NAME, args,
                SONGS_COLUMN_ID + " = " + id, null) > 0;
    }

    public void deleteTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(SONGS_TABLE_NAME, "1", null);
        db.close();
    }

    private boolean getBoolean(int value) {
        if (value == 0) {
            return false;
        }
        else {
            return true;
        }
    }

    private int getInteger(boolean value) {
        if (value) {
            return 1;
        }
        else {
            return 0;
        }
    }

    private boolean columnExists() {
        return false;
    }
}