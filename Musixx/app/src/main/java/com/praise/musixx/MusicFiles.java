package com.praise.musixx;

public class MusicFiles {
    String path;
    String album;
    String artist;
    String title;
    String duration;
    String id;


    public MusicFiles(String path, String album, String artist, String title, String duration, String id) {
        this.path = path;
        this.album = album;
        this.artist = artist;
        this.title = title;
        this.duration = duration;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
