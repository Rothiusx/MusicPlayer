package com.example.rothi.musicplayer.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.rothi.musicplayer.R;
import com.example.rothi.musicplayer.activity.interfaces.ActivityInterface;
import com.example.rothi.musicplayer.activity.interfaces.ServiceInterface;
import com.example.rothi.musicplayer.adapter.AdapterInterface;
import com.example.rothi.musicplayer.gui.ViewEditor;
import com.example.rothi.musicplayer.media.Song;
import com.example.rothi.musicplayer.service.Global;
import com.example.rothi.musicplayer.service.MusicService;
import com.example.rothi.musicplayer.service.MusicService.MusicBinder;
import com.example.rothi.musicplayer.database.SongsDatabase;
import com.example.rothi.musicplayer.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        ActivityInterface, ServiceInterface, AdapterInterface {

    FloatingActionButton fab;
    Toolbar toolbar;
    ListView songView;

    private static final String ACTIVITY_NAME = "_main";
    private Menu menu;
    private ArrayList<Song> songList;
    private ArrayList<Song> songListDefault;
    private ArrayList<Song> favoriteSongsList;
    private Intent musicIntent;
    private boolean musicBound = false;
    private boolean shuffleEnabled = false;
    private boolean repeatEnabled = false;
    private SharedPreferences preferences;
    private SharedPreferences.Editor preferencesEditor;
    private View lastView;
    private int lastSongId = 0;

    //TODO: CRASH WHEN NOT SWITCHING SONG ON FAVORITE LIST AND GOING BACK TO MAIN PAGE

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        preferences = getSharedPreferences("playback", Context.MODE_PRIVATE);
        shuffleEnabled = preferences.getBoolean("shuffle", false);
        repeatEnabled = preferences.getBoolean("repeat", false);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPlayer(view);
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        songView = findViewById(R.id.song_list);
        songView.setClickable(true);
        lastView = null;

        setPermissions();
        try {
            // Retrieve song information and place it in list.
            getSongListFromDevice();

        } catch (Exception e) {

            Log.e("PERMISSION", "Error with adding song list from the device: " +
                    e.getMessage(), e);
            e.printStackTrace();

            // Set permission to read storage and retrieve song information and place it in list.
            setPermissions();
            getSongListFromDevice();
        }

        initSongList();

        // Sort the song list alphabetically.
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        // Set a new adapter and display the songs.
        songAdapter.setSongAdapter(this, songList, ACTIVITY_NAME);
        songView.setAdapter(songAdapter);

        // Register context menu on song list
        registerForContextMenu(songView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Starts music service and binds to it
        if(musicIntent == null){
            musicIntent = new Intent(this, MusicService.class);
            bindService(musicIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(musicIntent);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPause() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_shuffle) {
            if(!shuffleEnabled) {
                shuffleEnabled = true;
                Global.musicService.setShuffle(shuffleEnabled);
                menu.getItem(0).setIcon(R.drawable.shuffle);
            }
            else {
                shuffleEnabled = false;
                Global.musicService.setShuffle(shuffleEnabled);
                menu.getItem(0).setIcon(R.drawable.shuffle_disabled);
            }
            savePreferences("shuffle", shuffleEnabled);
            return true;
        }
        if (id == R.id.action_repeat) {
            if(!repeatEnabled) {
                repeatEnabled = true;
                menu.getItem(1).setIcon(R.drawable.repeat);
            }
            else {
                repeatEnabled = false;
                menu.getItem(1).setIcon(R.drawable.repeat_disabled);
            }
            savePreferences("repeat", repeatEnabled);
            return true;
        }
        else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.action_stop_service) {
            // Stop the music service
            Global.musicService.pausePlayer();
            stopService(musicIntent);
            Global.musicService = null;
            return true;
        }
        else if (id == R.id.action_exit) {
            stopService(musicIntent);
            Global.musicService = null;
            System.exit(0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateActionMenu() {
        if(!shuffleEnabled) {
            menu.getItem(0).setIcon(R.drawable.shuffle_disabled);
        }
        else {
            menu.getItem(0).setIcon(R.drawable.shuffle);
        }
        if(!repeatEnabled) {
            menu.getItem(1).setIcon(R.drawable.repeat_disabled);
        }
        else {
            menu.getItem(1).setIcon(R.drawable.repeat);
        }
    }

    public void openPlayer(View view) {
        if(Global.musicService.getMediaPlayer()) {
            Intent intent = new Intent(getApplicationContext(), SongPlayerActivity.class);
            intent.putExtra("bound", musicBound);
            intent.putExtra("shuffle", shuffleEnabled);
            intent.putExtra("repeat", repeatEnabled);
            startActivityForResult(intent, 302);
        }
        else {
            Snackbar.make(view, "Currently playing no song", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        Song song = songList.get(Integer.parseInt(info.targetView.getTag().toString()));
        /*
        if (song.isFavorite()) {
            item.setTitle("Remove from Favorites");
        }
        else {
            item.setTitle("Add to Favorites");
        }
        */

        switch (item.getItemId()) {
            case R.id.context_play:
                songPicked(info.targetView);
                openPlayer(info.targetView);
                return true;

            case R.id.context_favorite:
                //addSongToList(info.targetView, favoriteSongsList);

                //addSongToDatabase(song);
                if (!song.isFavorite()) {
                    songList.get(Integer.parseInt(info.targetView.getTag().toString()))
                            .setFavorite(true);
                    addSongToDatabase(new Song(song.getId(), song.getTitle(), song.getArtist(),
                            true));

                    //info.targetView.setBackgroundResource(R.drawable.star);

                    //ViewEditor viewEditor = new ViewEditor(info.targetView);
                    //viewEditor.alphaAnimation(0.5f, 1.0f,1000);
                }
                else {
                    songList.get(Integer.parseInt(info.targetView.getTag().toString()))
                            .setFavorite(false);
                    deleteSongFromDatabase(new Song(song.getId(), song.getTitle(), song.getArtist(),
                            true));

                    //info.targetView.setBackgroundResource(R.drawable.star_disabled);
                }
                songAdapter.notifyDataSetChanged();
                return true;

            case R.id.context_search:
                String url = "https://goo.gl/search/" + song.getTitle();

                if (song.getArtist() != null && !song.getArtist().contains("unknown")) {
                    url += " " + song.getArtist();
                }

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
                return true;

            case R.id.context_watch:
                String yt = "vnd.youtube://www.youtube.com/results?search_query=" + song.getTitle();

                if (song.getArtist() != null && !song.getArtist().contains("unknown")) {
                    yt += " " + song.getArtist();
                }

                Intent intentYoutube = new Intent(Intent.ACTION_VIEW);
                intentYoutube.setData(Uri.parse(yt));
                startActivity(intentYoutube);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_recently_played) {
            // Show recently played songs
            Intent intent = new Intent(this, RecentlyPlayedActivity.class);
            intent.putExtra("bound", musicBound);
            startActivityForResult(intent, 110);
            return true;

        }
        else if (id == R.id.nav_favorite) {
            // Show favorite songs
            Intent intent = new Intent(this, FavoriteSongsActivity.class);
            intent.putExtra("bound", musicBound);
            startActivityForResult(intent, 120);
            return true;
        }
        else if (id == R.id.nav_playlists) {
            return true;
        }
        else if (id == R.id.nav_settings) {
            // Open settings
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;

        }
        else if (id == R.id.nav_login) {
            // Redirect to sign in page
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return true;

        }
        else if (id == R.id.nav_send) {
            return true;

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Get song list from device
    public void getSongListFromDevice() {
        // Retrieve song information
        songListDefault = new ArrayList<>();
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null,
                null, null);

        if(musicCursor != null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            // Add songs to list
            do {
                int thisId = musicCursor.getInt(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songListDefault.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
    }

    // Play selected song
    public void songPicked(View view) {
        int songIndex = Integer.parseInt(view.getTag().toString());

        //lastSong = songList.get(songIndex);
        //songAdapter.updateAdapter(songIndex);

        Global.musicService.setSong(songIndex);
        Global.musicService.playSong();

        /*
        if (lastView != null) {
            ViewEditor viewEditor = new ViewEditor(lastView, this);
            viewEditor.setTextView(R.id.song_title_main, false, false,
                    false, R.color.textColor);
        }

        ViewEditor viewEditor = new ViewEditor(view, this);
        viewEditor.setTextView(R.id.song_title_main, true, true,
                true, R.color.white);
        viewEditor.alphaAnimation(0.3f, 1.0f,1000);

        lastView = view;
        */

        //new UpdateAdapterUI().run();

        //songPicked = true;

        /*
        Toast.makeText(getApplicationContext(), view.getTag().toString(),
                Toast.LENGTH_SHORT).show();
                */

        /*
        Snackbar.make(view, "Playing: " + getCurrentSong().getTitle() + " - " +
                getCurrentSong().getArtist(), Snackbar.LENGTH_SHORT).setAction("Action",
                null).show();
                */
    }

    // Add selected song to a specific list
    private void addSongToList(View view, ArrayList<Song> songList) {
        songList.add(this.songList.get(Integer.parseInt(view.getTag().toString())));
    }

    // Add song from click to favorites
    public void addToFavorites(View view) {
        Song song = songList.get(Integer.parseInt(view.getTag().toString()));

        /*
        Toast.makeText(getApplicationContext(), view.getTag().toString(),
                Toast.LENGTH_LONG).show();
                */

        if (!song.isFavorite()) {

            songList.get(Integer.parseInt(view.getTag().toString())).setFavorite(true);
            addSongToDatabase(new Song(song.getId(), song.getTitle(), song.getArtist(),
                    true));

            view.setBackgroundResource(R.drawable.star);

            ViewEditor viewEditor = new ViewEditor(view);
            viewEditor.alphaAnimation(0.5f, 1.0f,1000);
        }
        else {

            songList.get(Integer.parseInt(view.getTag().toString())).setFavorite(false);
            deleteSongFromDatabase(new Song(song.getId(), song.getTitle(), song.getArtist(),
                    true));

            view.setBackgroundResource(R.drawable.star_disabled);
        }
    }

    // Add song to database
    public void addSongToDatabase(Song song) throws SQLiteConstraintException {
        SongsDatabase db = new SongsDatabase(this );

        if (db.findSong(song.getId()) == null) {
            db.insertSong(song);

            Toast.makeText(getApplicationContext(), "Added to Favorites",
                    Toast.LENGTH_LONG).show();
        }
    }

    // Delete song from database
    public void deleteSongFromDatabase(Song song) throws SQLiteConstraintException {
        SongsDatabase db = new SongsDatabase(this );

        if (db.findSong(song.getId()).getId() == song.getId()) {
            db.deleteSong(song.getId());

            Toast.makeText(getApplicationContext(), "Removed to Favorites", Toast.LENGTH_LONG).show();
        }
    }

    // Get song from and index
    private Song getSongByIndex(int songIndex) {
        return songList.get(songIndex);
    }

    // Get current song
    public Song getCurrentSong() {
        return Global.musicService.getSong();
    }

    // Initialize song lists
    public void initSongList() {
        if (songList == null) {
            songList = new ArrayList<>();
            songList = songListDefault;

            SongsDatabase db = new SongsDatabase(this);

            for (int i = 0; i < songList.size(); i++) {
                int id = songList.get(i).getId();

                if (db.findSong(id) == null) {
                    songList.get(i).setFavorite(false);
                }
                else {
                    songList.get(i).setFavorite(true);
                }
            }
        }

        if (favoriteSongsList == null) {
            favoriteSongsList = new ArrayList<>();
        }
    }

    public void setSongList(ArrayList<Song> songList) {
        this.songList = songList;
    }

    // Save shuffle and repeat state
    public void savePreferences(String key, Boolean value) {
        preferencesEditor = preferences.edit();
        preferencesEditor.putBoolean(key, value);
        preferencesEditor.apply();
    }

    // Connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder musicBinder = (MusicBinder)service;
            // Get service
            Global.musicService = musicBinder.getService();

            // Pass song list
            Global.musicService.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        try {
            switch (requestCode) {
                case 110:
                    break;

                case 120:

                    //TODO: OCCASIONAL TRANSITION CRASH

                    Toast.makeText(getApplicationContext(), "Back pressed", Toast.LENGTH_SHORT).show();
                    lastSongId = intent.getIntExtra("id", 0);
                    int currentIndex = -1;

                    if (lastSongId != 0) {
                        for (int i = 0; i < songList.size(); i++) {
                            if (songList.get(i).getId() == lastSongId) {
                                currentIndex = i;
                                break;
                            }
                        }
                    }

                    Global.musicService.setListIndex(0);
                    songAdapter.setSongAdapter(this, songList, "_main", currentIndex);
                    songView.setAdapter(songAdapter);
                    break;

                case 302:
                    shuffleEnabled = intent.getBooleanExtra("shuffle", false);
                    repeatEnabled = intent.getBooleanExtra("repeat", false);
                    updateActionMenu();
                    break;

                case 303:
                    break;
            }

        } catch (Exception e) {
            Log.e("ACTIVITY", "Error during activity result: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
        updateActionMenu();
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    protected void onDestroy() {
        // Stop the service when app is destroyed
        if(musicBound) {
            unbindService(musicConnection);
        }
        stopService(musicIntent);
        Global.musicService = null;
        super.onDestroy();
    }

    // Set permission to access storage
    private void setPermissions() {
        // Allows application to read storage of the device
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        100);
                return;
            }
        }
    }

    private class UpdateAdapterUI implements Runnable {
        @Override
        public void run() {
            MainActivity.this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    songAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}