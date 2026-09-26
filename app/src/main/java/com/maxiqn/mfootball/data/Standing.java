package com.maxiqn.mfootball.data;

import com.squareup.moshi.Json;

public class Standing {
    @Json(name = "position")
    public int position;
    
    @Json(name = "team")
    public Team team;
    
    @Json(name = "playedGames")
    public int playedGames;
    
    @Json(name = "won")
    public int won;
    
    @Json(name = "draw")
    public int draw;
    
    @Json(name = "lost")
    public int lost;
    
    @Json(name = "points")
    public int points;
    
    @Json(name = "goalDifference")
    public int goalDifference;
}
