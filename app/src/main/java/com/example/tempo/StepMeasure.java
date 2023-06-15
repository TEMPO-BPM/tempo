package com.example.tempo;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorEvent;
import android.widget.TextView;

import com.example.tempo.database.StepDB;
import com.example.tempo.domain.Step;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class StepMeasure {
    protected static final int TIME_STAMP_LENGTH = 10;
    private static final float MAX_SECONDS_BETWEEN_STEPS = 1;

    protected int currentSteps = 0;
    private long lastStepTimeStamp = 0;
    protected final float[] secondsBetweenSteps = new float[TIME_STAMP_LENGTH];
    private int timeStampIndex = 0;
    protected int stepsPerMinute = 0;
    private int bpmAccumulate = 0;
    StepDB stepDB;

    protected final TextView stepCountView;
    protected final TextView stepsPerMinuteView;

    StepMeasure(Context context, Activity activity){
        stepsPerMinuteView = activity.findViewById(R.id.stepsPerMinuteView);
        stepCountView = activity.findViewById(R.id.stepCountView);
        stepDB = StepDB.getInstance(context);
        loadSteps();
    }

    public void refreshStep(){
        stepCountView.setText(String.valueOf(currentSteps));
    }

    public void addStep(){
        currentSteps++;
        refreshStep();
    }

    public void refreshStepsPerMinute(){
        if (currentSteps > TIME_STAMP_LENGTH) {
            float timeStampTotalTime = 0;
            for(float times : secondsBetweenSteps){
                timeStampTotalTime += times;
            }
            stepsPerMinute = Math.round(60F / (timeStampTotalTime / TIME_STAMP_LENGTH));
            if (stepsPerMinute > 190){
                stepsPerMinute = 190;
            }
            stepsPerMinuteView.setText(String.valueOf(stepsPerMinute));

            bpmAccumulate += stepsPerMinute;
        }
    }

    public int getStepsPerMinute(){
        return stepsPerMinute;
    }

    public void recordTimeStamp(SensorEvent event){
        if (lastStepTimeStamp != 0) {
            float secondsFromLastStep = (float) (event.timestamp - lastStepTimeStamp) / 1000000000L;
            secondsBetweenSteps[timeStampIndex] = Math.min(secondsFromLastStep, MAX_SECONDS_BETWEEN_STEPS);
            timeStampIndex = getIndex(timeStampIndex + 1);
        }
        lastStepTimeStamp = event.timestamp;
    }

    private int getIndex(int val){
        if (val >= TIME_STAMP_LENGTH){
            return 0;
        }else if (val < 0){
            return TIME_STAMP_LENGTH-1;
        }else{
            return val;
        }
    }
    public int getAverageBPM(){
        return bpmAccumulate/(currentSteps - TIME_STAMP_LENGTH);
    }
    public void saveSteps(){
        LocalDateTime date = LocalDateTime.now(); //현재 시간
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Step step = new Step(currentSteps, dateStr);
        stepDB.stepDao().insertOne(step);
    }
    public void loadSteps(){
        LocalDateTime date = LocalDateTime.now(); //현재 시간
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        if(stepDB.stepDao().getStepByDate(dateStr) != null){
            currentSteps = stepDB.stepDao().getStepByDate(dateStr).getStep();
        }else{
            currentSteps = 0;
        }
        refreshStep();
    }
}
