package com.example.rothi.musicplayer.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.example.rothi.musicplayer.activity.MainActivity;
import com.example.rothi.musicplayer.R;
import com.example.rothi.musicplayer.adapter.AdapterInterface;
import com.example.rothi.musicplayer.media.MediaInterface;
import com.example.rothi.musicplayer.media.Song;

import java.util.ArrayList;
import java.util.Random;


public class MusicService extends Service implements MediaInterface, AdapterInterface {

    private MediaPlayer mp;
    private ArrayList<Song>[] songList;
    private ArrayList<Song> recentlyPlayedList;
    private int songIndex;
    private int listIndex;
    private final IBinder musicBinder = new MusicBinder();
    private boolean shuffle = false;
    private Random rand;

    public void onCreate(){
        // Create music service
        super.onCreate();

        // Initialize song position
        songIndex = 0;
        listIndex = 0;

        // Initialize song lists
        if (songList == null) {
            songList = new ArrayList[3];
        }
        if (recentlyPlayedList == null) {
            recentlyPlayedList = new ArrayList<>();
        }

        // Initialize random value for shuffle play
        rand = new Random();

        // Create player
        mp = new MediaPlayer();
        initMediaPlayer();
    }

    // Set media player properties
    public void initMediaPlayer(){
        mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mp.setOnPreparedListener(this);
        mp.setOnCompletionListener(this);
        mp.setOnErrorListener(this);
    }

    // Set song by index
    public void setSong(int songIndex){
        this.songIndex = songIndex;
    }

    // Previous list index
    public void prevList() {
        if(listIndex > 0) {
            listIndex--;
        }
    }

    // Next list index
    public void nextList() {
        if(listIndex < songList.length) {
            listIndex++;
        }
    }

    // Set list index
    public void setListIndex(int listIndex) {
        this.listIndex = listIndex;
    }

    // Get list index
    public int getListIndex() {
        return listIndex;
    }

    // Set current playlist
    public void setList(ArrayList<Song> songList){
        this.songList[listIndex] = songList;
    }

    // Get song information and play it
    public void playSong(){
        // Reset player
        mp.reset();

        // Get song
        Song song = songList[listIndex].get(songIndex);

        // Add song to recently played
        addToRecentlyPlayed(song);

        // Get song id
        int currentSong = song.getId();

        // Set uri
        Uri songUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media
                .EXTERNAL_CONTENT_URI, currentSong);

        try {
            mp.setDataSource(getApplicationContext(), songUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        songAdapter.updateAdapter(songIndex);
        //songAdapter.setLastIndex(songIndex);
        mp.prepareAsync();
        //songAdapter.updateAdapter();
    }

    // Add song to recently played
    public void addToRecentlyPlayed(Song song) {
        if (!recentlyPlayedList.contains(song)) {
            recentlyPlayedList.add(song);
        }
        else {
            recentlyPlayedList.remove(song);
            recentlyPlayedList.add(0, song);
        }
    }

    // Play previous song
    public void playPrev() {
        songIndex--;
        if(songIndex < 0) songIndex = songList[listIndex].size() - 1;
        playSong();
    }

    // Play next song
    public void playNext() {
        if(shuffle) {
            int newSong = songIndex;
            while(newSong == songIndex) {
                newSong = rand.nextInt(songList[listIndex].size());
            }
            songIndex = newSong;
        }
        else {
            songIndex++;
            if (songIndex >= songList[listIndex].size()) {
                songIndex = 0;
            }
        }
        playSong();
    }

    // Get song at current list and song index
    public Song getSong() {
        if (songIndex < songList[listIndex].size()) {
            return songList[listIndex].get(songIndex);
        }
        else {
            return null;
        }
    }

    public ArrayList<Song> getSongList() {
        return songList[listIndex];
    }

    // Get recently played song list
    public ArrayList<Song> getRecentlyPlayedList() {
        return recentlyPlayedList;
    }

    // Set shuffle for playback
    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    // Media player controls
    public int getCurrentProgress(){
        return mp.getCurrentPosition();
    }

    public int getDuration(){
        return mp.getDuration();
    }

    public boolean getMediaPlayer() {
        if(mp != null) {
            return true;
        }
        return false;
    }

    public boolean isServicePlaying(){
        return mp.isPlaying();
    }

    public void pausePlayer(){
        mp.pause();
    }

    public void seekTo(int position){
        mp.seekTo(position);
    }

    public void go(){
        mp.start();
    }

    public void setVolume(float leftVolume, float rightVolume) {
        mp.setVolume(leftVolume, rightVolume);
    }
    // End of media player controls

    //@androidx.annotation.Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mp.stop();
        mp.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mp.getCurrentPosition() > 0) {
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // Start playback
        mp.start();

        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.mipmap.music)
                .setTicker(getSong().getTitle())
                .setOngoing(true)
                .setContentTitle("Playing ")
                .setContentText(getSong().getTitle());
        Notification not = builder.build();

        startForeground(Global.NOTIFICATION_ID, not);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }
}
