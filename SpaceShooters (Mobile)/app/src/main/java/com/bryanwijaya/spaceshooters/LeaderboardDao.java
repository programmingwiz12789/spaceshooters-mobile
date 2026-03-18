package com.bryanwijaya.spaceshooters;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LeaderboardDao {
    @Query("SELECT * FROM leaderboard ORDER BY highscore DESC")
    List<Leaderboard> getLeaderboards();

    @Query("SELECT IFNULL(MAX(highscore), 0) FROM leaderboard")
    int getMaxHighScore();

    @Insert
    void insert(Leaderboard leaderboard);
}
