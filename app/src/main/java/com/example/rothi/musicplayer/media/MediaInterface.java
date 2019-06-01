package com.example.rothi.musicplayer.media;

import android.media.MediaPlayer;

public interface MediaInterface extends MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    @Override
    public void onCompletion(MediaPlayer mp);

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra);

    @Override
    public void onPrepared(MediaPlayer mp);
}
