package com.maxiqn.mfootball.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.maxiqn.mfootball.R;
import com.maxiqn.mfootball.data.Match;
import com.maxiqn.mfootball.network.ApiClient;
import com.maxiqn.mfootball.network.FootballApiService;
import com.maxiqn.mfootball.repository.MatchCacheManager;
import com.maxiqn.mfootball.repository.MatchRepository;
import com.maxiqn.mfootball.ui.adapter.LiveMatchAdapter;
import java.util.List;
import timber.log.Timber;

public class MatchesFragment extends Fragment {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private View emptyState;
    private LiveMatchAdapter adapter;
    private MatchRepository repository;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_matches, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupRepository();
        loadMatches();
    }
    
    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.matchesRecycler);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        emptyState = view.findViewById(R.id.emptyState);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LiveMatchAdapter(getContext(), new LiveMatchAdapter.OnMatchClickListener() {
            @Override
            public void onMatchClick(Match match) {
                Timber.d("Match clicked: %s vs %s", match.homeTeam.name, match.awayTeam.name);
            }
            
            @Override
            public void onFavoriteClick(Match match) {
                Timber.d("Favorite toggled for match");
            }
        });
        recyclerView.setAdapter(adapter);
        
        swipeRefresh.setOnRefreshListener(this::loadMatches);
    }
    
    private void setupRepository() {
        FootballApiService apiService = ApiClient.getClient().create(FootballApiService.class);
        MatchCacheManager cacheManager = new MatchCacheManager(getContext());
        repository = new MatchRepository(apiService, cacheManager);
    }
    
    private void loadMatches() {
        swipeRefresh.setRefreshing(true);
        repository.getLiveMatches(new MatchRepository.MatchesCallback() {
            @Override
            public void onSuccess(List<Match> matches) {
                if (getView() == null) return;
                swipeRefresh.setRefreshing(false);
                adapter.setMatches(matches);
                emptyState.setVisibility(matches.isEmpty() ? View.VISIBLE : View.GONE);
                Timber.d("Loaded %d matches", matches.size());
            }
            
            @Override
            public void onError(Throwable error) {
                if (getView() == null) return;
                swipeRefresh.setRefreshing(false);
                emptyState.setVisibility(View.VISIBLE);
                Timber.e(error, "Failed to load matches");
            }
        });
    }
}
