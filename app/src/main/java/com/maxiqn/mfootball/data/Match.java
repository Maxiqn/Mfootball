package com.maxiqn.mfootball.data;

import com.squareup.moshi.Json;
import java.io.Serializable;

public class Match implements Serializable {
    @Json(name = "id")
    public int id;
    
    @Json(name = "utcDate")
    public String utcDate;
    
    @Json(name = "status")
    public String status;
    
    @Json(name = "score")
    public Score score;
    
    @Json(name = "competition")
    public Competition competition;
    
    @Json(name = "homeTeam")
    public Team homeTeam;
    
    @Json(name = "awayTeam")
    public Team awayTeam;
    
    @Json(name = "stage")
    public String stage;
    
    @Json(name = "lastUpdated")
    public String lastUpdated;
    
    public static class Score {
        @Json(name = "home")
        public Integer home;
        
        @Json(name = "away")
        public Integer away;
        
        public String getDisplay() {
            return (home != null ? home : 0) + " - " + (away != null ? away : 0);
        }
    }
    
    public String getDisplayStatus() {
        if (status == null) return "SCHEDULED";
        switch (status.toUpperCase()) {
            case "LIVE":
            case "IN_PLAY":
                return "LIVE";
            case "PAUSED":
                return "HALF_TIME";
            case "FINISHED":
                return "FINISHED";
            case "SCHEDULED":
            default:
                return "SCHEDULED";
        }
    }
    
    public boolean isLive() {
        return status != null && (status.equalsIgnoreCase("LIVE") || status.equalsIgnoreCase("IN_PLAY"));
    }
}
