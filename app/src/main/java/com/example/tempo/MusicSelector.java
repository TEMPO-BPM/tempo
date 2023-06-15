package com.example.tempo;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tempo.database.SongDB;
import com.example.tempo.domain.Song;
import com.spotify.android.appremote.api.ImagesApi;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.ImageUri;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MusicSelector {
    private SongDB songDB = null;
    private final int MAX_BPM = 190;
    private final int MIN_BPM = 70;

    private final Context context;
    private final Activity activity;
    private StepMeasure stepMeasure;
    private Challenge challenge;
    private MediaPlayer mediaPlayer;
    private PlayerApi playerApi;
    private ImagesApi imagesApi;
    private SaveState saveState;
    List<Song> targetSongs = new ArrayList<>();
    int playCount;
    int currentSongIdx = 0;
    boolean is_challenge = false;
    String current_uri = "";
    boolean watching = false;

    List<Song> history = new ArrayList<>();

    private final ImageView albumArt;
    private final TextView songTitle;
    private final TextView singerName;
    private final TextView songBPM;
    private Random rd = new Random();

    ImageButton btnPlay;

    MusicSelector(Context context, Activity activity){
        this.context = context;
        this.activity = activity;
        albumArt = activity.findViewById(R.id.albumArt);
        songTitle = activity.findViewById(R.id.songTitle);
        singerName = activity.findViewById(R.id.singerName);
        btnPlay = activity.findViewById(R.id.btnPlay);
        songBPM = activity.findViewById(R.id.songBPM);
        songDB = SongDB.getInstance(context);
        saveState = new SaveState(context, activity);
        loadHistory();
    }

    public void setPlayerApi(PlayerApi player, ImagesApi imagesApi){
        this.playerApi = player;
        this.imagesApi = imagesApi;
        selectSong(100);
        playerApi.subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    final long position = playerState.playbackPosition;
                    if (watching && track != null && position == 0L && !track.uri.equals("spotify:track:"+current_uri)){
                        if(is_challenge){
                            challenge.finishSong();
                        }else{
                            selectSong(stepMeasure.getStepsPerMinute());
                        }
                    }
                });
    }

    public void setStepMeasure(StepMeasure step){
        stepMeasure = step;
    }

    public void setChallenge(Challenge cha){
        challenge = cha;
    }

    //분당 걸음 수에 따른 음악을 선택 및 재생 (디폴트 모드)
    public void selectSong(int stepsPerMinute){
        int songIdx;
        is_challenge = false;
        if (stepsPerMinute < MIN_BPM){
            stepsPerMinute = MIN_BPM;
        }else if(stepsPerMinute > MAX_BPM){
            stepsPerMinute = MAX_BPM;
        }
        List<Song> list = songDB.songDao().getSongbyBpm(stepsPerMinute);
        songIdx = rd.nextInt(list.size());
        String songURI = list.get(songIdx).getSpotifyUri();
        history.add(list.get(songIdx));
        history.remove(0);
        saveHistory();
        songBPM.setText("       BPM : " + String.valueOf(stepsPerMinute));
        playSong(songURI);
    }

    public void selectChallengeSong(){
        int songIdx = rd.nextInt(targetSongs.size());
        is_challenge = true;
        currentSongIdx = songIdx;
        String songURI = targetSongs.get(songIdx).getSpotifyUri();
        songBPM.setText("       BPM : " + String.valueOf(targetSongs.get(songIdx).getSongBpm()));
        playSong(songURI);
    }

    // level에 따른 음악 리스트를 지정하고 난수를 발생 시켜 songId를 도출
    public void setChallengeSongList(String level){
        if (!targetSongs.isEmpty()){
            targetSongs.clear();
        }
        int challenge_bpm_min;
        int challenge_bpm_max;
        switch (level) {
            case "LEVEL1":
                challenge_bpm_min = 90;
                challenge_bpm_max = 100;
                break;
            case "LEVEL2":
                challenge_bpm_min = 100;
                challenge_bpm_max = 110;
                break;
            case "LEVEL3":
                challenge_bpm_min = 110;
                challenge_bpm_max = 120;
                break;
            case "LEVEL4":
                challenge_bpm_min = 120;
                challenge_bpm_max = 130;
                break;
            case "LEVEL5":
                challenge_bpm_min = 130;
                challenge_bpm_max = 140;
                break;
            default:
                challenge_bpm_min = 140;
                challenge_bpm_max = 150;
        }
        for (int i = challenge_bpm_min; i < challenge_bpm_max; i++){
            targetSongs.addAll(songDB.songDao().getSongbyBpm(i));
        }
    }

    //mediaPlayer의 resource를 바꾸어 재생
    public void playSong(String songURI){
        watching = false;
        current_uri = songURI;
        playerApi.play("spotify:track:"+songURI);
        playerApi.subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track.uri.equals("spotify:track:"+songURI)){
                        watching = true;
                        songTitle.setText(playerState.track.name);
                        singerName.setText(playerState.track.artist.name);
                        imagesApi.getImage(playerState.track.imageUri).setResultCallback(albumArt::setImageBitmap);
                    }
                });
    }

    //챌린지 모드의 세부 스테이지에 따른 음악 재생 횟수 count
    public void playChallengeSong(String songURI) {

    }

    public void pauseSong(){
        playerApi.pause();
    }

    public void resumeSong(){
        playerApi.resume();
    }

    public PlayerApi getPlayerAPI(){
        return playerApi;
    }

    public void saveHistory(){
        saveState.saveHistory(history);
    }
    public void loadHistory(){
        history = saveState.loadHistory();
    }
    public void showHistory(){
        for(int i = 0; i < history.size(); i++){
            if (history.get(i) != null){
                Toast.makeText(context, String.format(Locale.KOREA, "%s", history.get(i).getSongTitle()), Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void setBPM(int bpm){
        songBPM.setText("       BPM : " + String.valueOf(bpm));
    }
}
