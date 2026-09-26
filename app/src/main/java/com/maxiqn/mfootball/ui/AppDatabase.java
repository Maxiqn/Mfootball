package com.maxiqn.mfootball.ui;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.maxiqn.mfootball.data.Match;

@Database(entities = {Match.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract MatchDao matchDao();
}
