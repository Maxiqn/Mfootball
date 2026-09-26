package com.maxiqn.mfootball.network;

import com.maxiqn.mfootball.data.ApiResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FootballApiService {
    @GET("/v4/competitions")
    Call<ApiResponse> getCompetitions(@Query("plan") String plan);
    
    @GET("/v4/competitions/{competitionId}/matches")
    Call<ApiResponse> getMatches(@Path("competitionId") int competitionId, @Query("status") String status);
    
    @GET("/v4/competitions/{competitionId}/standings")
    Call<ApiResponse> getStandings(@Path("competitionId") int competitionId);
    
    @GET("/v4/competitions/{competitionId}/matches")
    Call<ApiResponse> getMatchesForDate(@Path("competitionId") int competitionId, @Query("dateFrom") String dateFrom, @Query("dateTo") String dateTo);
}
