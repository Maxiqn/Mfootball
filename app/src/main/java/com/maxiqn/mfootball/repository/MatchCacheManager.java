package com.maxiqn.mfootball.repository;

import android.content.Context;
import android.content.SharedPreferences;
import com.maxiqn.mfootball.data.Match;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MatchCacheManager {
    private static final String PREFS_NAME = "mfootball_cache";
    private static final long CACHE_DURATION = 15 * 60 * 1000; // 15 minutes
    private final SharedPreferences prefs;
    private final Moshi moshi;
    
    public MatchCacheManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.moshi = new Moshi.Builder().build();
    }
    
    public void cacheMatches(int competitionId, List<Match> matches) {
        if (matches == null) return;
        Type type = Types.newParameterizedType(List.class, Match.class);
        String json = moshi.adapter(type).toJson(matches);
        prefs.edit()
            .putString("matches_" + competitionId, json)
            .putLong("timestamp_" + competitionId, System.currentTimeMillis())
            .apply();
    }
    
    public List<Match> getCachedMatches(int competitionId) {
        long timestamp = prefs.getLong("timestamp_" + competitionId, 0);
        if (System.currentTimeMillis() - timestamp > CACHE_DURATION) {
            return new ArrayList<>();
        }
        
        String json = prefs.getString("matches_" + competitionId, null);
        if (json == null) return new ArrayList<>();
        
        try {
            Type type = Types.newParameterizedType(List.class, Match.class);
            List<Match> matches = moshi.adapter(type).fromJson(json);
            return matches != null ? matches : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    public void cacheLiveMatches(List<Match> matches) {
        cacheMatches(0, matches);
    }
    
    public List<Match> getCachedLiveMatches() {
        return getCachedMatches(0);
    }
    
    public void clearCache() {
        prefs.edit().clear().apply();
    }
}
