package com.example.tempo;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.tempo.database.SongDB;
import com.example.tempo.database.StepDB;
import com.example.tempo.domain.Step;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.PlayerRestrictions;
import com.spotify.protocol.types.Track;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SongDB songDB = null;
    private StepDB stepDB = null;

    int stepsPerMinute = 0;
    boolean isChallenge = false;

    StepMeasure stepMeasure;
    AutoPause autoPause;
    MusicSelector musicSelector;

    ConstraintLayout steps; //운동량

    SensorManager sensorManager;
    Sensor stepCountSensor;
    Challenge challenge;

    TextView title;
    ImageButton btnPlay;
    ImageButton btnNext;
    ImageButton btnPrev;
    //디버깅 버튼/////////////
    boolean debugMode = false;
    Button btnSkip;
//    Button btnReset;
    ////////////////////////
    CompoundButton challengeSwitch;
    private ActivityResultLauncher<Intent> resultLauncher;

    private static final String CLIENT_ID = "972cc589581944968b50e0f1c5fd1014";
    private static final String REDIRECT_URI = "https://example.com/callback";
    private SpotifyAppRemote mSpotifyAppRemote;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Spotify 연결
        connectSpotify();

        //database 연결
        songDB = SongDB.getInstance(this);

        //////////걸음수저장할때날짜
        LocalDateTime date = LocalDateTime.now(); //현재 시간
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        /////////
        stepDB = StepDB.getInstance(this);
        //////걸음수DB 초기값셋팅/////////
              for(int i = 1; i<=31; i++){
                  Random random = new Random();
                  int randomNumber = random.nextInt(100) + 100;
                  Step step = new Step(randomNumber, "2023/05/" + String.valueOf(i));
                  stepDB.stepDao().insertOne(step);
              }


        stepMeasure = new StepMeasure(MainActivity.this, this);
        musicSelector = new MusicSelector(MainActivity.this, this);
        autoPause = new AutoPause(MainActivity.this, this, musicSelector);
        musicSelector.setStepMeasure(stepMeasure);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        //디버깅 버튼////////////
        title = findViewById(R.id.title);
        btnSkip = findViewById(R.id.btnSkip);
//        btnReset = findViewById(R.id.btnReset);
        ///////////////////////
        challenge = new Challenge(MainActivity.this, this);
        challengeSwitch = findViewById(R.id.challengeSwitch);
        challenge.exitChallengeMode();
        challengeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                isChallenge = true;
                challenge.enterChallengeMode(musicSelector, stepMeasure);
                autoPause.stopPauseTimer();
            }
            else {
                isChallenge = false;
                challenge.exitChallengeMode();
                musicSelector.selectSong(stepMeasure.getStepsPerMinute());
            }
        });

        // 활동 퍼미션 체크
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }

        // 디바이스에 걸음 센서의 존재 여부 체크
        if (stepCountSensor == null) {
            Toast.makeText(this, "No Step Sensor", Toast.LENGTH_SHORT).show();
        }

        //next 버튼 클릭시 곡을 선택
        btnNext.setOnClickListener(v -> {
            if (isChallenge) {
                musicSelector.selectChallengeSong();
            }
            else {
                musicSelector.selectSong(stepMeasure.getStepsPerMinute());
            }
        });

        //재생, 중지 버튼
        btnPlay.setOnClickListener(v -> {
            autoPause.stopPauseTimer();
            musicSelector.getPlayerAPI().getPlayerState().setResultCallback(playerState -> {
                if (!playerState.isPaused){
                    musicSelector.pauseSong();
                }else{
                    musicSelector.resumeSong();
                }
            });
        });

        //운동량 레이아웃 클릭시 Analysis 액티비티로 전환
        steps = findViewById(R.id.steps);

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode()==1){
                    if(!isChallenge){
                        challengeSwitch.performClick();
                    }
                }
                else if(result.getResultCode()==2){
                    if(isChallenge){
                        challengeSwitch.performClick();
                    }
                    assert result.getData() != null;
                    musicSelector.setBPM(result.getData().getIntExtra("bpm", 999));
                    musicSelector.playSong(result.getData().getStringExtra("uri"));
                }
            }
        });

        steps.setOnClickListener(v -> {
            stepMeasure.saveSteps();
            Intent intent = new Intent(MainActivity.this, AnalysisActivity.class);
            resultLauncher.launch(intent);
        });

        btnSkip.setOnClickListener(v -> {
            if(isChallenge){
                challenge.finishSong();
            }
        });

        title.setOnClickListener(v ->{
            if(debugMode){
                debugMode = false;
                btnSkip.setVisibility(View.GONE);
            }else{
                debugMode = true;
                btnSkip.setVisibility(View.VISIBLE);
            }
        });
    }

    public void onStart() {
        super.onStart();
        if(stepCountSensor !=null) {
            sensorManager.registerListener(this,stepCountSensor,SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    public void onRestart(){
        super.onRestart();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        // 걸음 센서 이벤트 발생시
        if(event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR){
            if(event.values[0]==1.0f){
                // 센서 이벤트가 발생할때 마다 걸음수 증가
                stepMeasure.addStep();
                stepMeasure.recordTimeStamp(event);
                stepMeasure.refreshStepsPerMinute();
                stepsPerMinute = stepMeasure.getStepsPerMinute();
                if (isChallenge){
                    challenge.watchStep();
                }
                // 자동 일시정지 상태라면 다시 음악을 재생
                if (autoPause.getAutoPaused()){
                    musicSelector.getPlayerAPI().resume();
                    btnPlay.setImageResource(android.R.drawable.ic_media_pause);
                }
                // 자동 일시정지 타이머 초기화
                if(!isChallenge) {
                    autoPause.startPauseTimer(musicSelector.getPlayerAPI(), btnPlay);
                }
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void connectSpotify(){
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now you can start interacting with App Remote
                        connected();

                    }

                    public void onFailure(Throwable throwable) {
                        Log.e("MyActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        stepMeasure.saveSteps();
    }

    protected void onStop() {
        super.onStop();
        stepMeasure.saveSteps();
    }

    private void connected() {
        // Play a playlist
        //mSpotifyAppRemote.getPlayerApi().play();

        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity", track.name + " by " + track.artist.name);
                    }
                });
        musicSelector.setPlayerApi(mSpotifyAppRemote.getPlayerApi(), mSpotifyAppRemote.getImagesApi());
        mSpotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
            if (!playerState.isPaused){
                btnPlay.setImageResource(android.R.drawable.ic_media_pause);
            }
            if (playerState.isPaused){
                btnPlay.setImageResource(android.R.drawable.ic_media_play);
            }
        });
    }
}