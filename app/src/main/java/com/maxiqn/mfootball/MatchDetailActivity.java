package com.maxiqn.mfootball;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MatchDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_detail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Spieldetails");
        }

        TextView titleView = findViewById(R.id.detailTitle);
        TextView dateView = findViewById(R.id.detailDate);
        TextView leagueView = findViewById(R.id.detailLeague);
        TextView statusView = findViewById(R.id.detailStatus);
        TextView teamsView = findViewById(R.id.detailTeams);

        String homeTeam = getIntent().getStringExtra("homeTeam");
        String awayTeam = getIntent().getStringExtra("awayTeam");
        String competitionName = getIntent().getStringExtra("competitionName");
        String status = getIntent().getStringExtra("status");
        String date = getIntent().getStringExtra("date");

        titleView.setText("Match Details");
        teamsView.setText(homeTeam + " vs " + awayTeam);
        leagueView.setText(competitionName);
        statusView.setText(status);
        dateView.setText(MainActivity.formatDate(date));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
