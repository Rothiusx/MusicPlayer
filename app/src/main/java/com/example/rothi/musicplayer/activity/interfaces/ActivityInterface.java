package com.example.rothi.musicplayer.activity.interfaces;

import android.database.sqlite.SQLiteConstraintException;
import android.view.View;

import com.example.rothi.musicplayer.media.Song;

public interface ActivityInterface {

    public void initSongList();

    public void songPicked(View view);

    public void openPlayer(View view);

    public void savePreferences(String key, Boolean value);

    public void addSongToDatabase(Song song) throws SQLiteConstraintException;

    public void deleteSongFromDatabase(Song song) throws SQLiteConstraintException;
}
