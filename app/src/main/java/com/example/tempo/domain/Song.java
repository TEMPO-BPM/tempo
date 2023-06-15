package com.example.tempo.domain;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Song {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name="songTitle")
    private String songTitle;
    @ColumnInfo(name="artistName")
    private String artistName;
    @ColumnInfo(name="spotifyUri")
    private String spotifyUri;
    @ColumnInfo(name="songBpm")
    private int songBpm;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getSpotifyUri() {
        return spotifyUri;
    }

    public void setSpotifyUri(String spotifyUri) {
        this.spotifyUri = spotifyUri;
    }

    public int getSongBpm() {
        return songBpm;
    }

    public void setSongBpm(int songBpm) {
        this.songBpm = songBpm;
    }
}
