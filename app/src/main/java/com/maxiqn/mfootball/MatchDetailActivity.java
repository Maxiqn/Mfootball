package com.maxiqn.mfootball;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MatchDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_detail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.match_details));
        }

        TextView titleView = findViewById(R.id.detailTitle);
        TextView dateView = findViewById(R.id.detailDate);
        TextView leagueView = findViewById(R.id.detailLeague);
        TextView statusView = findViewById(R.id.detailStatus);
        TextView teamsView = findViewById(R.id.detailTeams);

        String homeTeam = safeExtra("homeTeam", "Heimteam");
        String awayTeam = safeExtra("awayTeam", "Auswärtsteam");
        String competitionName = safeExtra("competitionName", "Wettbewerb");
        String status = safeExtra("status", "Nicht verfügbar");
        String date = safeExtra("date", "TBD");

        titleView.setText(R.string.match_details);
        teamsView.setText(getString(R.string.matchup_format, homeTeam, awayTeam));
        leagueView.setText(competitionName);
        statusView.setText(status);
        dateView.setText(MainActivity.formatDate(date));
    }

    private String safeExtra(String key, String fallback) {
        String value = getIntent().getStringExtra(key);
        return TextUtils.isEmpty(value) ? fallback : value;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
