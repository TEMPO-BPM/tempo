package com.example.tempo.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import static androidx.room.OnConflictStrategy.REPLACE;
import com.example.tempo.domain.Song;
import java.util.List;

@Dao
public interface SongDao {

    @Query("SELECT * FROM Song WHERE songBpm=:bpm")
    List<Song> getSongbyBpm(int bpm);

    @Query("SELECT * FROM Song")
    List<Song> getAll();

    @Insert(onConflict = REPLACE)
    void insertOne(Song song);

    @Delete
    void deleteOne(Song song);
}
