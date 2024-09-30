package com.example.test_project;

public class CurrentArtist {
    private static CurrentArtist instance;
    private int artistId;

    private CurrentArtist() {}

    public static CurrentArtist getInstance() {
        if (instance == null) {
            instance = new CurrentArtist();
        }
        return instance;
    }

    public void setArtistId(int artistId) {
        this.artistId = artistId;
    }

    public int getArtistId() {
        return artistId;
    }
}