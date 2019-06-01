package com.example.rothi.musicplayer.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.example.rothi.musicplayer.R;
import com.example.rothi.musicplayer.activity.interfaces.ActivityInterface;
import com.example.rothi.musicplayer.activity.interfaces.ServiceInterface;
import com.example.rothi.musicplayer.adapter.AdapterInterface;
import com.example.rothi.musicplayer.service.Global;
import com.example.rothi.musicplayer.settings.SettingsActivity;
import com.example.rothi.musicplayer.media.Song;
import com.example.rothi.musicplayer.database.SongsDatabase;

import java.util.ArrayList;
import java.util.Collections;

public class FavoriteSongsActivity extends AppCompatActivity
        implements ActivityInterface, ServiceInterface, AdapterInterface {

    Toolbar toolbar;
    ListView songView;
    FloatingActionButton fab;

    //private SongAdapter songAdapter;
    private static final String ACTIVITY_NAME = "_favorite";
    private ArrayList<Song> songList;
    private boolean musicBound = false;
    private Song lastSong;
    private boolean songPicked = false;
    //private int currentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_favorite_songs);

        Intent intent = getIntent();
        musicBound = intent.getBooleanExtra("bound", false);

        toolbar = findViewById(R.id.favorite_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fab = findViewById(R.id.favorite_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPlayer(view);
            }
        });

        //ActionBar ab = getSupportActionBar();
        //ab.setDisplayHomeAsUpEnabled(true);

        //mp.setOnCompletionListener(this);
        songView = findViewById(R.id.favorite_song_list);

        // Initialize song list for favorite songs
        if (songList == null) {
            songList = new ArrayList<>();
        }

        // Get favorite songs list as serializable
        //Intent intent = getIntent();
        //songList = (ArrayList<Song>)intent.getSerializableExtra("favorite");

        //lastSong = null;
        initSongList();

        // Reverse song list
        Collections.reverse(songList);

        // Find if currently playing song is in favorite song list
        int currentIndex = - 1;
        for (int i = 0; i < songList.size(); i++) {
            if (songList.get(i).getId() == getCurrentSong().getId()) {
                currentIndex = i;
                break;
            }
        }

        // Show songs in list
        Global.musicService.setListIndex(2);
        //songAdapter = new SongAdapter(this, songList, "_favorite", currentIndex);
        songAdapter.setSongAdapter(this, songList, ACTIVITY_NAME, currentIndex);
        songView.setAdapter(songAdapter);

        //Toast.makeText(getApplicationContext(), songAdapter.getItem(3).toString() + "", Toast.LENGTH_LONG).show();

        // Pass song list
        Global.musicService.setList(songList);
    }

    // Load playlist information from database
    public void initSongList() {
        SongsDatabase db = new SongsDatabase(this);
        songList = db.loadSongList();
    }

    // Play selected song
    public void songPicked(View view) {
        int songIndex = Integer.parseInt(view.getTag().toString());
        lastSong = songList.get(songIndex);

        songAdapter.updateAdapter(songIndex);

        Global.musicService.setSong(songIndex);
        Global.musicService.playSong();

        //ViewEditor viewEditor = new ViewEditor(view, this);
        //viewEditor.setTextView(R.id.song_title_main, true, true, true, R.color.white);
        //viewEditor.alphaAnimation(0.3f, 1.0f,1000);

        songPicked = true;

        /*
        Toast.makeText(getApplicationContext(), view.getTag().toString(),
                Toast.LENGTH_SHORT).show();
                */

        /*
        Snackbar.make(view, "Playing: " + getCurrentSong().getTitle() + " - " +
                getCurrentSong().getArtist(), Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
                */

    }

    // Get current song
    public Song getCurrentSong() {
        return Global.musicService.getSong();
    }

    // Open currently playing song in player
    public void openPlayer(View view) {
        if(Global.musicService.getMediaPlayer()) {
            Intent intent = new Intent(getApplicationContext(), SongPlayerActivity.class);
            intent.putExtra("bound", musicBound);
            //intent.putExtra("shuffle", shuffleEnabled);
            //intent.putExtra("repeat", repeatEnabled);
            startActivity(intent);
        }
        else {
            Snackbar.make(view, "Currently playing no song", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_favorite_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_favorite_clear_db) {
            SongsDatabase db = new SongsDatabase(this);
            db.deleteTable();
            initSongList();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void savePreferences(String key, Boolean value) {

    }

    public void addSongToDatabase(Song song) throws SQLiteConstraintException {

    }

    public void deleteSongFromDatabase(Song song) throws SQLiteConstraintException {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.favorite, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (songPicked) {
            Intent intent = new Intent();
            intent.putExtra("id", lastSong.getId());
            setResult(120, intent);
        }
        else {
            Global.musicService.setListIndex(0);
        }
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }
}
