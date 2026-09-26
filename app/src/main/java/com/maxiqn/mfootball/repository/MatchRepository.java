package com.maxiqn.mfootball.repository;

import com.maxiqn.mfootball.data.ApiResponse;
import com.maxiqn.mfootball.data.Match;
import com.maxiqn.mfootball.network.FootballApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;
import timber.log.Timber;

public class MatchRepository {
    private final FootballApiService apiService;
    private final MatchCacheManager cacheManager;
    
    public MatchRepository(FootballApiService apiService, MatchCacheManager cacheManager) {
        this.apiService = apiService;
        this.cacheManager = cacheManager;
    }
    
    public void getLiveMatches(MatchesCallback callback) {
        // First try cache
        List<Match> cached = cacheManager.getCachedLiveMatches();
        if (!cached.isEmpty()) {
            callback.onSuccess(cached);
        }
        
        // Then fetch fresh
        apiService.getMatches(0, "LIVE").enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Match> matches = response.body().matches;
                    cacheManager.cacheLiveMatches(matches);
                    callback.onSuccess(matches);
                    Timber.d("Loaded %d live matches", matches != null ? matches.size() : 0);
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Timber.e(t, "Failed to fetch live matches");
                callback.onError(t);
            }
        });
    }
    
    public void getMatchesForCompetition(int competitionId, MatchesCallback callback) {
        List<Match> cached = cacheManager.getCachedMatches(competitionId);
        if (!cached.isEmpty()) {
            callback.onSuccess(cached);
        }
        
        apiService.getMatches(competitionId, null).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Match> matches = response.body().matches;
                    cacheManager.cacheMatches(competitionId, matches);
                    callback.onSuccess(matches);
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                callback.onError(t);
            }
        });
    }
    
    public interface MatchesCallback {
        void onSuccess(List<Match> matches);
        void onError(Throwable error);
    }
}
