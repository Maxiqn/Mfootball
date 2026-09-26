package com.maxiqn.mfootball.data;

import com.squareup.moshi.Json;
import java.util.List;

public class ApiResponse {
    @Json(name = "matches")
    public List<Match> matches;
    
    @Json(name = "competitions")
    public List<Competition> competitions;
    
    @Json(name = "standings")
    public List<Standing> standings;
}
