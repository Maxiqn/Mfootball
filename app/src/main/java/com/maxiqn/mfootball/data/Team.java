package com.maxiqn.mfootball.data;

import com.squareup.moshi.Json;

public class Team {
    @Json(name = "id")
    public int id;
    
    @Json(name = "name")
    public String name;
    
    @Json(name = "shortName")
    public String shortName;
    
    @Json(name = "tla")
    public String tla;
    
    @Json(name = "crest")
    public String crest;
}
