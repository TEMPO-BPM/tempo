package com.example.tempo.database;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.tempo.domain.Step;

@Dao
public interface StepDao {
    @Query("SELECT * FROM Step WHERE date=:date")
    Step getStepByDate(String date);

    @Insert(onConflict = REPLACE)
    void insertOne(Step step);

    @Delete
    void deleteOne(Step step);

    @Query("DELETE FROM Step WHERE date=:date")
    void deleteOneByDate(String date);

    @Update
    void updateOne(Step step);
}
