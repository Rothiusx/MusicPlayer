package com.example.rothi.musicplayer.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.example.rothi.musicplayer.R;
import com.example.rothi.musicplayer.service.Global;
import com.example.rothi.musicplayer.media.Song;
import com.example.rothi.musicplayer.adapter.SongAdapter;
import com.example.rothi.musicplayer.gui.ViewEditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public class RecentlyPlayedActivity extends AppCompatActivity {

    ListView songView;
    Toolbar toolbar;
    FloatingActionButton fab;

    private SongAdapter songAdapter;
    private boolean refreshThread = true;
    private ArrayList<Song> songList;
    private boolean musicBound = false;
    private Song lastSong;
    private boolean songPicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_recently_played);

        Intent intent = getIntent();
        musicBound = intent.getBooleanExtra("bound", false);

        toolbar = findViewById(R.id.recently_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fab = findViewById(R.id.recently_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPlayer(view);
            }
        });

        //ActionBar ab = getSupportActionBar();
        //ab.setDisplayHomeAsUpEnabled(true);

        songView = findViewById(R.id.recently_played_list);

        // Initialize song list for recently played tracks
        if (songList == null) {
            songList = new ArrayList<>();
        }

        // Get recently played song list from service
        songList = Global.musicService.getRecentlyPlayedList();

        // Reverse song list
        Collections.reverse(songList);

        // Show songs in list
        Global.musicService.setListIndex(1);
        songAdapter = new SongAdapter(this, songList, "_recent");
        songView.setAdapter(songAdapter);

        // Pass song list
        Global.musicService.setList(songList);

        initListUpdater();
    }

    // Handler for refreshing recently played list
    @SuppressLint("HandlerLeak")
    private Handler refreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // Create new list and get message
            ArrayList<Song> newList;
            newList = (ArrayList) msg.obj;

            //Toast.makeText(getApplicationContext(), "List Refreshed", Toast.LENGTH_SHORT).show();
            songAdapter.updateAdapter(newList);

            super.handleMessage(msg);
        }
    };

    // Start thread for refreshing list
    private void initListUpdater() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (refreshThread && Global.musicService.getMediaPlayer()) {
                    try {
                        Message msg = new Message();
                        msg.obj = Global.musicService.getRecentlyPlayedList();
                        refreshHandler.sendMessage(msg);
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Log.e("RECENT RUNNABLE", "Interrupted exception in thread: " +
                                e.getMessage(), e);
                        e.getStackTrace();
                    }
                }
            }
        }).start();
    }

    // Play selected song
    public void songPicked(View view) {
        int songIndex = Integer.parseInt(view.getTag().toString());
        lastSong = songList.get(songIndex);

        songAdapter.updateAdapter(0);

        Global.musicService.setSong(songIndex);
        Global.musicService.playSong();

        ViewEditor viewEditor = new ViewEditor(view, this);
        viewEditor.alphaAnimation(0.3f, 1.0f,1000);

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
    private Song getCurrentSong() {
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

    // Remove duplicate items from list
    private void removeDuplicates(ArrayList<Song> songList) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            songList.stream().distinct().collect(Collectors.toList());
        }
    }

    @Override
    public void onBackPressed() {
        if (songPicked) {
            Intent intent = new Intent();
            intent.putExtra("id", lastSong.getId());
            setResult(110, intent);
        }
        else {
            Global.musicService.setListIndex(0);
        }
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        refreshThread = false;
        super.onDestroy();
    }
}
