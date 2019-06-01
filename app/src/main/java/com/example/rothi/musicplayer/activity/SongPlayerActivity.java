package com.example.rothi.musicplayer.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.rothi.musicplayer.R;
import com.example.rothi.musicplayer.media.MusicController;
import com.example.rothi.musicplayer.media.Song;
import com.example.rothi.musicplayer.service.Global;

public class SongPlayerActivity extends Activity implements MediaController.MediaPlayerControl {

    ImageView albumCover;
    TextView songTitle;
    TextView songArtist;
    TextView elapsedTimeLabel;
    TextView remainingTimeLabel;
    SeekBar positionBar;
    SeekBar volumeBar;
    Button playButton;
    Button forwardButton;
    Button backwardButton;
    Button repeatButton;
    Button shuffleButton;

    private int totalTime;
    private boolean musicBound = false;
    private boolean shuffleEnabled = false;
    private boolean repeatEnabled = false;
    private MusicController controller;
    private SharedPreferences preferences;
    private SharedPreferences.Editor preferencesEditor;
    private Thread musicThread;
    private static int pausedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_song_player);

        preferences = getSharedPreferences("playback", Context.MODE_PRIVATE);
        Intent intent = getIntent();
        musicBound = intent.getBooleanExtra("bound", false);
        shuffleEnabled = intent.getBooleanExtra("shuffle", preferences
                .getBoolean("shuffle", false));
        repeatEnabled = intent.getBooleanExtra("repeat", preferences
                .getBoolean("repeat", false));

        albumCover = findViewById(R.id.album_cover);
        songTitle = findViewById(R.id.song_name);
        songArtist = findViewById(R.id.song_name_artist);
        elapsedTimeLabel = findViewById(R.id.elapsedTimeLabel);
        remainingTimeLabel = findViewById(R.id.remainingTimeLabel);
        positionBar = findViewById(R.id.positionBar);
        volumeBar = findViewById(R.id.volumeBar);
        playButton = findViewById(R.id.playButton);
        forwardButton = findViewById(R.id.forwardButton);
        backwardButton = findViewById(R.id.backwardButton);
        repeatButton = findViewById(R.id.repeatButton);
        shuffleButton = findViewById(R.id.shuffleButton);

        setController();

        initButtons();
        initBars();
        initThreads();

        updateSong();

        musicThread.start();

        setVolume(1.0f);
    }

    // Handler for progress bar and time labels
    @SuppressLint("HandlerLeak")
    private Handler positionHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            int currentPosition = msg.what;

            // Update progression bar and time labels
            String elapsedTime;
            String remainingTime;

            if (isPlaying()) {
                positionBar.setProgress(currentPosition);
                elapsedTime = createTimeLabel(currentPosition);
                remainingTime = createTimeLabel(totalTime - currentPosition);
            }
            else {
                positionBar.setProgress(pausedTime);
                elapsedTime = createTimeLabel(pausedTime);
                remainingTime = createTimeLabel(totalTime - pausedTime);
            }

            elapsedTimeLabel.setText(elapsedTime);
            remainingTimeLabel.setText("- " + remainingTime);

            super.handleMessage(msg);
        }
    };

    // Media buttons listeners
    private void initButtons() {
        updatePlayPause();
        updateShuffleRepeat();

        playButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!isPlaying()) {
                            // Resume player
                            Global.musicService.go();
                            playButton.setBackgroundResource(R.drawable.pause);
                            //initBars();
                        }
                        else {
                            // Stop player
                            pausedTime = getCurrentPosition();
                            Global.musicService.pausePlayer();
                            playButton.setBackgroundResource(R.drawable.play);
                        }
                    }
                }
        );

        forwardButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playNext();
                        updateSong();
                        playButton.setBackgroundResource(R.drawable.pause);
                    }
                }
        );

        backwardButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPrev();
                        updateSong();
                        playButton.setBackgroundResource(R.drawable.pause);
                    }
                }
        );

        repeatButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!repeatEnabled) {
                            repeatEnabled = true;
                            repeatButton.setBackgroundResource(R.drawable.repeat);
                        }
                        else {
                            repeatEnabled = false;
                            repeatButton.setBackgroundResource(R.drawable.repeat_disabled);
                        }
                        Intent intent = new Intent();
                        savePreferences("repeat", repeatEnabled);
                        intent.putExtra("repeat", repeatEnabled);
                        intent.putExtra("shuffle", shuffleEnabled);
                        setResult(302, intent);
                    }
                }
        );

        shuffleButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!shuffleEnabled) {
                            shuffleEnabled = true;
                            Global.musicService.setShuffle(shuffleEnabled);
                            shuffleButton.setBackgroundResource(R.drawable.shuffle);
                        }
                        else {
                            shuffleEnabled = false;
                            Global.musicService.setShuffle(shuffleEnabled);
                            shuffleButton.setBackgroundResource(R.drawable.shuffle_disabled);
                        }
                        savePreferences("shuffle", shuffleEnabled);
                        Intent intent = new Intent();
                        intent.putExtra("repeat", shuffleEnabled);
                        intent.putExtra("shuffle", shuffleEnabled);
                        setResult(302, intent);
                    }
                }
        );
    }

    // Volume and progress bar listeners
    private void initBars() {
        // Position Bar
        positionBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(fromUser) {
                            Global.musicService.seekTo(progress);
                            positionBar.setProgress(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );

        // Volume Bar
        volumeBar.setProgress(100);
        volumeBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        float volume = progress / 100f;
                        setVolume(volume);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );
    }

    // Initialize threads
    public void initThreads() {
        musicThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(Global.musicService.getMediaPlayer()) {
                    try {
                        Message msg = new Message();
                        msg.what = getCurrentPosition();
                        positionHandler.sendMessage(msg);
                        Thread.sleep(500);

                    } catch (InterruptedException e) {
                        Log.e("PLAYER RUNNABLE", "Interrupted exception in thread: " +
                                e.getMessage(), e);
                        e.getStackTrace();
                    }
                }
            }
        });
    }

    // Create time labels for handler
    private String createTimeLabel(int time) {
        String timeLabel;
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if(sec < 10) {
            timeLabel += "0";
        }
        timeLabel += sec;

        return timeLabel;
    }

    // Update play and pause icons
    private void updatePlayPause() {
        if(!isPlaying()) {
            playButton.setBackgroundResource(R.drawable.play);
        }
        else {
            playButton.setBackgroundResource(R.drawable.pause);
        }
    }

    // Update shuffle and repeat buttons
    private void updateShuffleRepeat() {
        if(!shuffleEnabled) {
            Global.musicService.setShuffle(shuffleEnabled);
            shuffleButton.setBackgroundResource(R.drawable.shuffle_disabled);
        }
        else {
            Global.musicService.setShuffle(shuffleEnabled);
            shuffleButton.setBackgroundResource(R.drawable.shuffle);
        }

        if(!repeatEnabled) {
            repeatButton.setBackgroundResource(R.drawable.repeat_disabled);
        }
        else {
            repeatButton.setBackgroundResource(R.drawable.repeat);
        }
    }

    // Update song information
    private void updateSong() {
            totalTime = getDuration();
            positionBar.setMax(totalTime);

            Song song = getCurrentSong();
            //albumCover.setImageResource(song.getAlbumCover());
            songTitle.setText(song.getTitle());
            songTitle.setSelected(true);
            songArtist.setText(song.getArtist());
    }

    // Get currently playing song
    private Song getCurrentSong() {
        return Global.musicService.getSong();
    }

    // Set volume
    private void setVolume(float volume) {
        Global.musicService.setVolume(volume, volume);
    }

    // Play next
    private void playNext(){
        Global.musicService.playNext();
        controller.show(0);
    }

    // Play previous
    private void playPrev(){
        Global.musicService.playPrev();
        controller.show(0);
    }

    // Set media player controller
    private void setController(){
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }

    // Media player controls
    @Override
    public void start() {
        Global.musicService.go();
    }

    @Override
    public void pause() {
        Global.musicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(Global.musicService != null && musicBound && Global.musicService.getMediaPlayer()) {
            return Global.musicService.getDuration();
        }
        else {
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        if(Global.musicService != null && musicBound && isPlaying()) {
            return Global.musicService.getCurrentProgress();
        }
        else {
            return 0;
        }
    }

    @Override
    public void seekTo(int pos) {
        Global.musicService.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        if(Global.musicService != null && musicBound){
            return Global.musicService.isServicePlaying();
        }
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
    // End of media player controls

    // Save shuffle and repeat state
    private void savePreferences(String key, Boolean value) {
        preferencesEditor = preferences.edit();
        preferencesEditor.putBoolean(key, value);
        preferencesEditor.apply();
    }
}
