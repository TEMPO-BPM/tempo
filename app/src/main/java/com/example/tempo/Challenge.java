package com.example.tempo;

import static androidx.appcompat.content.res.AppCompatResources.getDrawable;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static java.lang.Character.getNumericValue;

import androidx.constraintlayout.widget.ConstraintLayout;

import org.w3c.dom.Text;

public class Challenge {

    private final Context context;
    private final Activity activity;
    private ImageView icon;

    private ImageView runIcon;
    private ImageView musicIcon;

    private final TextView appTitle;
    private final TextView stepTitle;
    private final TextView musicTitle;
    private final TextView challengeTitle;
    private final TextView todayMission;
    private final TextView todayLevel;
    private final TextView todayStage;
    private final TextView songTitle;
    private final TextView singerName;
    private final TextView songBPM;
    private final TextView textView8;
    private final TextView textView10;
    private final TextView autoPauseView;
    TextView[] views;

    private final LinearLayout challengeLayout;
    private final TextView remainingGoal;
    private final TextView remainingGoalLabel;
    private final TextView stepCountView;
    private final TextView stepsPerMinuteView;
    private final SaveState saveState; //데이터 저장
    private final ConstraintLayout steps;
    private String targetLevel; //레벨
    private String targetStage; //세부 스테이지
    private int targetBPM = 85; //목표 BPM
    private int songsLeft = 0; // 남은 챌린지 곡 수
    private int songsBeforeRest = 0; // 휴식 전에 남은 챌린지 곡 수 (!)0이라면 현재 곡이 휴식곡이라는 것을 의미
    private static final int CHALLENGE_SUCCESS_PERCENTAGE = 50; //성공 커트라인 (%)
    private static final int CHALLENGE_LEVEL_CLEAR_PERCENTAGE = 0;
    private static final int BPM_RANGE = 10; //목표 BPM 범위 (+-)
    private int stepsOutBPM = 0; //목표 BPM 범위에서 벗어난 걸음 수
    private int stepsInBPM = 0; //목표 BPM 범위 안에 들어간 걸음 수
    private MusicSelector musicSelector;
    private StepMeasure stepMeasure;
    int successCount = 0;
    int failedCount = 0;



    private final Map<String, String> ChallengeMission = Map.of(
            "LEVEL1", "BPM 90-100",
            "LEVEL2", "BPM 100-110",
            "LEVEL3", "BPM 110-120",
            "LEVEL4", "BPM 120-130",
            "LEVEL5", "BPM 130-140",
            "-1", "2곡 뛰기\n휴식 없음",
            "-2", "2곡 뛰고\n1곡 쉬고",
            "-3", "3곡 뛰고\n1곡 쉬고",
            "-4", "3곡 뛰고\n1곡 쉬고"
    );

    Challenge(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        saveState = new SaveState(context, activity);
        loadData();

        challengeLayout = activity.findViewById(R.id.challengeLayout);
        appTitle = activity.findViewById(R.id.title);
        stepTitle = activity.findViewById(R.id.stepTitle);
        musicTitle = activity.findViewById(R.id.musicTitle);
        songTitle = activity.findViewById(R.id.songTitle);
        singerName = activity.findViewById(R.id.singerName);
        songBPM = activity.findViewById(R.id.songBPM);
        textView8 = activity.findViewById(R.id.textView8);
        textView10 = activity.findViewById(R.id.textView10);
        autoPauseView = activity.findViewById(R.id.autoPauseView);

        runIcon = activity.findViewById(R.id.imageView);
        musicIcon = activity.findViewById(R.id.imageView2);

        challengeTitle = activity.findViewById(R.id.challengeTitle);
        todayMission = activity.findViewById(R.id.todayMission);
        todayLevel = activity.findViewById(R.id.todayLevel);
        todayStage = activity.findViewById(R.id.todayStage);
        stepsPerMinuteView = activity.findViewById(R.id.stepsPerMinuteView);
        stepCountView = activity.findViewById(R.id.stepCountView);
        remainingGoal = activity.findViewById(R.id.remainingGoal);
        remainingGoalLabel = activity.findViewById(R.id.remainingGoalLabel);
        steps = activity.findViewById(R.id.steps);
        icon = activity.findViewById(R.id.level4_1);

        views = new TextView[]{appTitle, stepTitle, musicTitle, stepCountView, stepsPerMinuteView, challengeTitle, songTitle, singerName, songBPM, textView8, textView10, autoPauseView };

    }
    public void enterChallengeMode(MusicSelector musicSel, StepMeasure step){
        challengeLayout.setVisibility(View.VISIBLE);

        for(int i = 0; i < views.length; i++){
            views[i].setTextColor(Color.parseColor("#5A6EFF"));
        }

        runIcon.setImageResource(R.drawable.brun);
        musicIcon.setImageResource(R.drawable.bmusic);

        todayMission.setText(String.format("%s%s", targetLevel, targetStage));
        todayLevel.setText(ChallengeMission.get(targetLevel));
        todayStage.setText(ChallengeMission.get(targetStage));
        musicSelector = musicSel;
        stepMeasure = step;
        musicSelector.setChallenge(this);

        setSongCount();
        resetInOutStep();
        startSong();
    }

    public void setSongCount(){
        switch(targetStage){
            case("-1"):
                songsLeft = 2;
                songsBeforeRest = -1;
                break;
            case("-2"):
                songsLeft = 5;
                songsBeforeRest = 2;
                break;
            case("-3"):
            case("-4"):
                songsLeft = 7;
                songsBeforeRest = 3;
                break;
        }
        refreshRemaining();
    }

    public void refreshRemaining(){
        if (songsBeforeRest > 0) {
            remainingGoalLabel.setText("다음 휴식까지");
            remainingGoal.setText(String.format(Locale.KOREA, "%d%s", songsBeforeRest, "곡"));
        }else{
            remainingGoalLabel.setText("챌린지 완료까지");
            remainingGoal.setText(String.format(Locale.KOREA, "%d%s", songsLeft, "곡"));
        }
    }

    public void exitChallengeMode(){
        challengeLayout.setVisibility(View.GONE);

        for(int i = 0; i < views.length; i++){
            views[i].setTextColor(Color.parseColor("#7a5cf9"));
        }

        runIcon.setImageResource(R.drawable.prun);
        musicIcon.setImageResource(R.drawable.pmusic);


    }

    public void startSong(){
        musicSelector.setChallengeSongList(targetLevel);
        musicSelector.selectChallengeSong();
    }

    public void finishSong() {
        float successRate = (float) successCount / (float) (successCount + failedCount) * 100;
        if (songsBeforeRest == 0) { //방금 끝난 곡이 휴식 곡이었다면
            Toast.makeText(context, "휴식 곡 종료", Toast.LENGTH_SHORT).show();
        } else if (songsBeforeRest == 1) { //방금 끝난 곡이 휴식 직전 곡이었다면
            Toast.makeText(context, "휴식 곡 시작", Toast.LENGTH_SHORT).show();
        } else {
            verifyChallenge();
        }
        resetInOutStep();
        musicSelector.selectChallengeSong();
        songsLeft--;
        songsBeforeRest--;
        refreshRemaining();
        if (songsLeft == 0) {
            musicSelector.pauseSong();
            if (successRate >= CHALLENGE_LEVEL_CLEAR_PERCENTAGE) {
                if (Objects.equals(targetLevel, "LEVEL1") && Objects.equals(targetStage, "-4")) {
                    targetLevel = "LEVEL2";
                    targetStage = "-1";
                }
                else if(Objects.equals(targetLevel, "LEVEL2") && Objects.equals(targetStage, "-4")){
                    targetLevel = "LEVEL3";
                    targetStage = "-1";
                }
                else if(Objects.equals(targetLevel, "LEVEL3") && Objects.equals(targetStage, "-4")) {
                    targetLevel = "LEVEL4";
                    targetStage = "-1";
                }
                else{
                    switch (targetStage){
                        case ("-1"):
                            targetStage = "-2";
                            break;
                        case ("-2"):
                            targetStage = "-3";
                            break;
                        case ("-3"):
                            targetStage = "-4";
                            break;

                    }
                }
                setIcons();
                todayMission.setText(String.format("%s%s", targetLevel, targetStage));
                todayLevel.setText(ChallengeMission.get(targetLevel));
                todayStage.setText(ChallengeMission.get(targetStage));
                setTargetBPM(targetLevel);
                setSongCount();
                resetInOutStep();
                startSong();
            }
            else{
                icon.setImageResource(R.drawable.failedicon);
            }
            saveData();
        }
    }
    public void resetInOutStep(){
        stepsInBPM = 0;
        stepsOutBPM = 0;
    }

    public void watchStep(){
        if (songsBeforeRest == 0){ //현재 곡이 휴식 곡이라면
            stepsPerMinuteView.setTextColor(Color.parseColor("#5A6EFF"));
        }else{
            int stepsPerMinute = stepMeasure.getStepsPerMinute();
            if (Math.abs(targetBPM - stepsPerMinute) > BPM_RANGE){ //현재 걸음 수가 목표 BPM 범위 바깥이라면
                stepsPerMinuteView.setTextColor(Color.parseColor("#7a5cf9")); //약간 더 주황색
                stepsOutBPM += 1;
            }else{
                stepsPerMinuteView.setTextColor(Color.parseColor("#5A6EFF"));
                stepsInBPM += 1;
            }
        }
    }

    public void verifyChallenge(){
        float score = (float)stepsInBPM / Math.max(1,(float)(stepsOutBPM + stepsInBPM)) * 100;
        if (score >= CHALLENGE_SUCCESS_PERCENTAGE){
            Toast.makeText(context, String.format(Locale.KOREA, "%.1f%s", score, "점: 성공"), Toast.LENGTH_SHORT).show();
            successCount += 1;
        }else{
            Toast.makeText(context, String.format(Locale.KOREA, "%.1f%s", score, "점: 실패"), Toast.LENGTH_SHORT).show();
            failedCount += 1;
        }
    }

    private void loadData(){
        String[] data = saveState.loadChallenge();
        targetLevel = data[0];
        targetStage = data[1];
        setTargetBPM(targetLevel);
        setIcons();
    }
    private void saveData(){
        saveState.saveChallenge(targetLevel, targetStage);
    }
    public void resetLevel(){
        targetLevel = "LEVEL1";
        targetStage = "-1";
        todayMission.setText(String.format("%s%s", targetLevel, targetStage));
        todayLevel.setText(ChallengeMission.get(targetLevel));
        todayStage.setText(ChallengeMission.get(targetStage));
        setTargetBPM(targetLevel);
        setIcons();
        setSongCount();
        resetInOutStep();
        startSong();
    }
    public void setTargetBPM(String targetLevel){
        switch(targetLevel){
            case("LEVEL1"):
                targetBPM = 95;
                break;
            case("LEVEL2"):
                targetBPM = 105;
                break;
            case("LEVEL3"):
                targetBPM = 115;
                break;
            case("LEVEL4"):
                targetBPM = 125;
                break;
        }
    }

    public void setIcons(){
        for(int level = 1; level < 6; level++){
            for(int stage = 1; stage < 5; stage++){
                String iconID = "level"+ level +"_"+stage;
                int resID = context.getResources().getIdentifier(iconID, "id", context.getPackageName());
                icon = activity.findViewById(resID);
                if (level < getNumericValue(targetLevel.charAt(5))){
                    icon.setImageResource(R.drawable.successicon);
                }else if (level == getNumericValue(targetLevel.charAt(5)) && stage < getNumericValue(targetStage.charAt(1))){
                    icon.setImageResource(R.drawable.successicon);
                }else{
                    icon.setImageResource(R.drawable.failedicon);
                }
            }
        }
    }
}


