package com.example.rothi.musicplayer.media;

import com.example.rothi.musicplayer.adapter.AdapterInterface;

import java.io.Serializable;

public class Song implements Serializable {

    private int id;
    private String title;
    private String artist;
    private String album;
    private String cover;
    private boolean favorite;

    public Song() {
    }

    public Song(int id, String title, String artist) {
        this.id = id;
        this.title = title;
        this.artist = artist;
    }

    public Song(int id, String title, String artist, boolean favorite) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.favorite = favorite;
    }

    public Song(int id, String title, String artist, String album) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
    }

    public Song(int id, String title, String artist, String album, String cover) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.cover = cover;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getCover() {
        return cover;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}