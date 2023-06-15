package com.example.tempo.domain;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Date;
import java.time.LocalDateTime;

@Entity
public class Step {

    @ColumnInfo(name="step")
    private int step;

    @ColumnInfo(name="date")
    @PrimaryKey
    @NonNull
    private String date;

    public Step(int step, String date) {
        this.step = step;
        this.date = date;
    }



    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}