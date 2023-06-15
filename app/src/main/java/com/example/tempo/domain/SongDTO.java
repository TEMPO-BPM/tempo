package com.example.tempo.domain;

import org.json.JSONException;
import org.json.JSONObject;

public class SongDTO {
    private String songTitle;
    private String songUri;
    private String artistName;
    private int songBpm;

    public SongDTO(String songTitle, String songUri, String artistName, int songBpm) {
        this.songTitle = songTitle;
        this.songUri = songUri;
        this.artistName = artistName;
        this.songBpm = songBpm;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public String getSongUri() {
        return songUri;
    }

    public String getArtistName() {
        return artistName;
    }

    public int getSongBpm() {
        return songBpm;
    }

    public static SongDTO fromJson(JSONObject jsonObject) {
        try {
            String songTitle = jsonObject.getString("song_title");
            String songUri = jsonObject.getString("song_uri");
            int songBpm = Integer.parseInt(jsonObject.getString("tempo"));
            JSONObject artistObject = jsonObject.getJSONObject("artist");
            String artistName = artistObject.getString("name");

            return new SongDTO(songTitle, songUri, artistName, songBpm);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

}

