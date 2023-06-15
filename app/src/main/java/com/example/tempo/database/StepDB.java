package com.example.tempo.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.tempo.domain.Step;

@Database(entities = {Step.class}, version = 1)
public abstract class StepDB extends RoomDatabase {
    private static StepDB INSTANCE = null;

    public abstract StepDao stepDao();

    public static StepDB getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            StepDB.class, "step.db")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

}
