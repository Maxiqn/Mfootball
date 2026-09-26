package com.maxiqn.mfootball.ui;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.maxiqn.mfootball.data.Match;
import java.util.List;

@Dao
public interface MatchDao {
    @Query("SELECT * FROM matches WHERE status = 'LIVE' OR status = 'IN_PLAY' ORDER BY utcDate ASC")
    List<Match> getLiveMatches();
    
    @Query("SELECT * FROM matches WHERE competition_id = :competitionId ORDER BY utcDate ASC")
    List<Match> getMatchesByCompetition(int competitionId);
    
    @Insert
    void insertMatches(List<Match> matches);
    
    @Update
    void updateMatch(Match match);
    
    @Query("DELETE FROM matches WHERE lastUpdated < :timestamp")
    void deleteOldMatches(long timestamp);
}
