package com.example.tempo;

import static android.icu.number.NumberRangeFormatter.RangeIdentityFallback.RANGE;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.tempo.database.StepDB;
import com.example.tempo.domain.Song;
import com.example.tempo.domain.Step;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class AnalysisActivity extends AppCompatActivity {
    private StepDB stepDB = null;
    private SaveState saveState;
    private TextView textChallenge;
    private TextView avgSteps;
    private TextView compareSteps;
    private ConstraintLayout btnChallenge;

    private LinearLayout history1;
    private LinearLayout history2;
    private LinearLayout history3;

    private TextView bpm1;
    private TextView bpm2;
    private TextView bpm3;

    ///앨범아트123
    private ImageView albumArt1;
    private ImageView albumArt2;
    private ImageView albumArt3;

    private TextView songTitle1;
    private TextView songTitle2;
    private TextView songTitle3;

    LinearLayout[] histories;
    TextView[] bpms;
    TextView[] songTitles;
    ImageView[] albumArts;


    int weeklySteps = 0;
    int lastWeek = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analysis);
        textChallenge = findViewById(R.id.challengeLevel);
        avgSteps = findViewById(R.id.avgSteps);
        compareSteps = findViewById(R.id.textView5);
        btnChallenge = findViewById(R.id.btnGotoChallenge);

        saveState = new SaveState(AnalysisActivity.this, this);
        drawGraph();
        showHistory();
        showChallengeLevel();
        setResult(0);
        btnChallenge.setOnClickListener(v -> {
            setResult(1);
            finish();
        });
    }

    private void drawGraph() {
        LineChart lineChart = findViewById(R.id.stepChart);
        stepDB = StepDB.getInstance(this);

        LocalDate date = LocalDate.now();
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        Step[] steps = new Step[7];

        for (int i = 6; i >= 0; i--) {
            Step temp = stepDB.stepDao().getStepByDate(dateStr);
            ////////추가
            weeklySteps += temp.getStep();
            //////
            steps[i] = new Step(temp.getStep(), temp.getDate());
            date = date.minusDays(1);
            dateStr = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        }

        for (int i = 6; i >= 0; i--) {
            Step temp = stepDB.stepDao().getStepByDate(dateStr);
            ////////추가
            lastWeek += temp.getStep();
            //////
            date = date.minusDays(1);
            dateStr = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        }
        weeklySteps /= 7; //최근 7일 평균 걸음 수
        avgSteps.setText(String.format("평균 %d걸음 걸었어요.", weeklySteps));

        lastWeek /= 7;
        int diff = Math.abs(weeklySteps - lastWeek);
        if(weeklySteps >= lastWeek) {
            compareSteps.setText(String.format("평균 %d걸음 증가했어요.", diff));
        }
        else{
            compareSteps.setText(String.format("평균 %d걸음 감소했어요.", diff));
        }
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE); // x축 데이터 표시 위치
        xAxis.setGranularity(1f);
        xAxis.setSpaceMin(1f); // Chart 맨 왼쪽 간격 띄우기
        xAxis.setSpaceMax(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < 7) {
                    return steps[index].getDate().substring(5);
                } else {
                    return ""; // 범위를 벗어나는 경우 빈 문자열 반환
                }
            }
        });

        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setDrawAxisLine(false);
        YAxis yAxis = lineChart.getAxisRight();
        yAxis.setDrawLabels(false);
        yAxis.setDrawAxisLine(false);

        ArrayList<Entry> entries = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            entries.add(new Entry(i, steps[i].getStep()));
        }

        LineDataSet lineDataSet = new LineDataSet(entries, "걸음수");
        lineDataSet.setColor(Color.parseColor("#7a5cf9"));
        lineDataSet.setLineWidth(3f);
        lineDataSet.setValueTextSize(12f);
        lineDataSet.setCircleColor(Color.parseColor("#7a5cf9"));
        lineChart.getDescription().setEnabled(false);
        LineData lineData = new LineData(lineDataSet);

        lineChart.setData(lineData);
    }

    private void showHistory () {
        history1 = findViewById(R.id.history1);
        history2 = findViewById(R.id.history2);
        history3 = findViewById(R.id.history3);

        bpm1 = findViewById(R.id.bpm1);
        bpm2 = findViewById(R.id.bpm2);
        bpm3 = findViewById(R.id.bpm3);

        albumArt1 = findViewById(R.id.albumArt1);
        albumArt2 = findViewById(R.id.albumArt2);
        albumArt3 = findViewById(R.id.albumArt3);

        songTitle1 = findViewById(R.id.songTitle1);
        songTitle2 = findViewById(R.id.songTitle2);
        songTitle3 = findViewById(R.id.songTitle3);

        histories = new LinearLayout[]{history3, history2, history1};
        bpms = new TextView[]{bpm3, bpm2, bpm1};
        songTitles = new TextView[]{songTitle3, songTitle2, songTitle1};
        albumArts = new ImageView[]{albumArt3, albumArt2, albumArt1};

        List<Song> history = saveState.loadHistory();

        for (int i = 0; i < 3; i++) {
            if (history.get(i) != null) {
                Song song = history.get(i);
                bpms[i].setText(String.valueOf(song.getSongBpm()));
                songTitles[i].setText(song.getSongTitle());
                histories[i].setOnClickListener(v -> {
                    Intent intent = new Intent();

                    intent.putExtra("uri", song.getSpotifyUri());
                    intent.putExtra("bpm", song.getSongBpm());
                    setResult(2, intent);
                    finish();
                });
            }
        }
    }

    private void showChallengeLevel () {
        String[] data = saveState.loadChallenge();
        textChallenge.setText("현재 " + data[0] + data[1] + " 도전 중");
    }
}
