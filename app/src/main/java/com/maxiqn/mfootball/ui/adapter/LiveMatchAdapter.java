package com.maxiqn.mfootball.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.maxiqn.mfootball.R;
import com.maxiqn.mfootball.data.Match;
import java.util.ArrayList;
import java.util.List;

public class LiveMatchAdapter extends RecyclerView.Adapter<LiveMatchAdapter.MatchViewHolder> {
    private final List<Match> matches;
    private final OnMatchClickListener listener;
    private final Context context;
    
    public interface OnMatchClickListener {
        void onMatchClick(Match match);
        void onFavoriteClick(Match match);
    }
    
    public LiveMatchAdapter(Context context, OnMatchClickListener listener) {
        this.context = context;
        this.matches = new ArrayList<>();
        this.listener = listener;
    }
    
    public void setMatches(List<Match> newMatches) {
        matches.clear();
        if (newMatches != null) {
            matches.addAll(newMatches);
        }
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_live_match, parent, false);
        return new MatchViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        Match match = matches.get(position);
        holder.bind(match, listener, context);
    }
    
    @Override
    public int getItemCount() {
        return matches.size();
    }
    
    static class MatchViewHolder extends RecyclerView.ViewHolder {
        private final TextView competitionBadge;
        private final TextView competitionName;
        private final TextView matchTime;
        private final TextView homeTeamName;
        private final TextView scoreDisplay;
        private final TextView statusBadge;
        private final TextView awayTeamName;
        private final View viewDetails;
        private final View addFavorite;
        
        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            competitionBadge = itemView.findViewById(R.id.competitionBadge);
            competitionName = itemView.findViewById(R.id.competitionName);
            matchTime = itemView.findViewById(R.id.matchTime);
            homeTeamName = itemView.findViewById(R.id.homeTeamName);
            scoreDisplay = itemView.findViewById(R.id.scoreDisplay);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            awayTeamName = itemView.findViewById(R.id.awayTeamName);
            viewDetails = itemView.findViewById(R.id.viewDetails);
            addFavorite = itemView.findViewById(R.id.addFavorite);
        }
        
        void bind(Match match, OnMatchClickListener listener, Context context) {
            // Set competition info
            if (match.competition != null) {
                String code = match.competition.code != null ? match.competition.code : "";
                competitionBadge.setText(code);
                competitionName.setText(match.competition.name);
            }
            
            // Set teams
            homeTeamName.setText(match.homeTeam != null ? match.homeTeam.name : "Home");
            awayTeamName.setText(match.awayTeam != null ? match.awayTeam.name : "Away");
            
            // Set score
            String score = match.score != null ? match.score.getDisplay() : "- vs -";
            scoreDisplay.setText(score);
            
            // Set status with color
            String status = match.getDisplayStatus();
            statusBadge.setText(status);
            
            int statusColor = match.isLive() ? 
                ContextCompat.getColor(context, R.color.accent_red) :
                ContextCompat.getColor(context, R.color.text_secondary);
            statusBadge.setBackgroundColor(statusColor);
            
            // Set match time
            if (match.utcDate != null) {
                matchTime.setText(extractTimeFromDate(match.utcDate));
            }
            
            // Click listeners
            itemView.setOnClickListener(v -> listener.onMatchClick(match));
            viewDetails.setOnClickListener(v -> listener.onMatchClick(match));
            addFavorite.setOnClickListener(v -> listener.onFavoriteClick(match));
        }
        
        private String extractTimeFromDate(String utcDate) {
            try {
                return utcDate.substring(11, 16);
            } catch (Exception e) {
                return "";
            }
        }
    }
}
