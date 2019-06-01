package com.example.rothi.musicplayer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rothi.musicplayer.R;
import com.example.rothi.musicplayer.activity.MainActivity;
import com.example.rothi.musicplayer.gui.ViewEditor;
import com.example.rothi.musicplayer.database.SongsDatabase;
import com.example.rothi.musicplayer.media.Song;
import com.example.rothi.musicplayer.service.MusicService;

import java.util.ArrayList;

public class SongAdapter extends BaseAdapter {

    private ArrayList<Song> songList;
    private LayoutInflater songInflater;
    private String type;
    private Context context;
    private int lastIndex = - 1;

    public SongAdapter() {
    }

    public void setSongAdapter(Context context, ArrayList<Song> songList, String type) {
        this.context = context;
        this.songList = songList;
        this.songInflater = LayoutInflater.from(context);
        this.type = type;
    }

    public void setSongAdapter(Context context, ArrayList<Song> songList, String type, int lastIndex) {
        this.context = context;
        this.songList = songList;
        this.songInflater = LayoutInflater.from(context);
        this.type = type;
        this.lastIndex = lastIndex;
    }

    public SongAdapter(Context context, ArrayList<Song> songList, String type) {
        this.context = context;
        this.songList = songList;
        this.songInflater = LayoutInflater.from(context);
        this.type = type;
    }

    public SongAdapter(Context context, ArrayList<Song> songList, String type, int lastIndex) {
        this.context = context;
        this.songList = songList;
        this.songInflater = LayoutInflater.from(context);
        this.type = type;
        this.lastIndex = lastIndex;
    }

    @Override
    public int getCount() {
        return songList.size();
    }

    @Override
    public Object getItem(int position) {
        return songList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView title;
        TextView artist;
        ImageView favorite;

        int layoutId = context.getResources().getIdentifier("song" + type,
                "layout", context.getPackageName());
        int titleId = context.getResources().getIdentifier("song_title" + type,
                "id", context.getPackageName());
        int artistId = context.getResources().getIdentifier("song_artist" + type,
                "id", context.getPackageName());
        int favoriteId = context.getResources().getIdentifier("song_like_button" + type,
                "id", context.getPackageName());


        convertView = songInflater.inflate(layoutId, parent, false);

        // Get title and artist views.
        title = convertView.findViewById(titleId);
        artist = convertView.findViewById(artistId);
        favorite = convertView.findViewById(favoriteId);

        // Get song using current position in adapter
        Song song = songList.get(position);

        // Set title and artist text

        if (song.getArtist() != null && song.getArtist().contains("unknown")) {
            if(song.getTitle().contains("-")) {
                title.setText(song.getTitle().substring(song.getTitle().indexOf("-") + 2));
                artist.setText(song.getTitle().substring(0,
                        song.getTitle().indexOf("-")));
            }
            else {
                title.setText(song.getTitle());
                artist.setText(null);
            }
        }
        else {
            title.setText(song.getTitle());
            artist.setText(song.getArtist());
        }

        // Set favorite button status
        if (updateSong(song).isFavorite()) {
            favorite.setBackgroundResource(R.drawable.star);
        }
        else {
            favorite.setBackgroundResource(R.drawable.star_disabled);
        }

        // Update view if song is currently playing
        /*
        if (Global.musicService != null && Global.musicService.getSong() != null) {
            if (Global.musicService.isServicePlaying() &&
                    Global.musicService.getSong().getId() == song.getId() &&
                    Global.musicService.getSongList() == songList) {

                selectView(convertView, titleId);
            }
            else {
                deselectView(convertView, titleId);
            }
        }
        */
        // Set position as tag
        favorite.setTag(position);
        convertView.setTag(position);

        // Run only on favorite song list startup
        if (lastIndex > - 1 && position == lastIndex) {
            selectView(convertView, titleId);
        }
        else if (lastIndex > - 1) {
            deselectView(convertView, titleId);
        }

        return convertView;
    }

    // Set song index
    public void setLastIndex(int lastIndex) {
        this.lastIndex = lastIndex;
    }

    // Update list
    public void updateAdapter() {
        notifyDataSetChanged();
    }

    public void updateAdapter(int lastIndex) {
        this.lastIndex = lastIndex;
        notifyDataSetChanged();
    }

    public void updateAdapter(ArrayList<Song> songList) {
        this.songList = songList;
        notifyDataSetChanged();
    }

    private void selectView(View view, int id) {
        ViewEditor viewEditor = new ViewEditor(view, context);
        viewEditor.setTextView(id, true, true,
                true, R.color.white);
        //viewEditor.alphaAnimation(0.3f, 1.0f,1000);
    }

    private void deselectView(View view, int id) {
        ViewEditor viewEditor = new ViewEditor(view, context);
        viewEditor.setTextView(id, false, false,
                false, R.color.textColor);
    }

    private void animateView(View view) {
        ViewEditor viewEditor = new ViewEditor(view, context);
        viewEditor.alphaAnimation(0.3f, 1.0f,1000);
    }

    private Song updateSong(Song song) {

        SongsDatabase db = new SongsDatabase(context);

        if (db.findSong(song.getId()) == null) {
            return song;
        }
        else {
            return db.findSong(song.getId());
        }
    }

    private void setOnClickListener(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    class Task1 extends AsyncTask<Void, Void, String> {

        private View view;
        private int id;

        public Task1(View view, int id) {
            this.view = view;
            this.id = id;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(Void... arg0)
        {
            ViewEditor viewEditor = new ViewEditor(view, context);
            viewEditor.setTextView(id, true, true,
                    true, R.color.white);
            viewEditor.alphaAnimation(0.3f, 1.0f,1000);
            return "OK";
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

        }
    }
}