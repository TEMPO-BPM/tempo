package com.example.tempo.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.tempo.domain.Song;

@Database(entities = {Song.class}, version = 1)
public abstract class SongDB extends RoomDatabase {
    private static SongDB INSTANCE = null;

    public abstract SongDao songDao();

    public static SongDB getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            SongDB.class, "song_ver2.db")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .createFromAsset("database/song_ver2.db")
                    .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}