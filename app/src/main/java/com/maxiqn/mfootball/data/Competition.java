package com.maxiqn.mfootball.data;

import com.squareup.moshi.Json;

public class Competition {
    @Json(name = "id")
    public int id;
    
    @Json(name = "name")
    public String name;
    
    @Json(name = "code")
    public String code;
    
    @Json(name = "type")
    public String type;
    
    @Json(name = "emblem")
    public String emblem;
}
