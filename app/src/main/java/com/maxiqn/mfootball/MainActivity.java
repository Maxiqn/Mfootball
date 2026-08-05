package com.maxiqn.mfootball;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Color;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView fixturesRecycler;
    private RecyclerView competitionsRecycler;
    private TextView fixturesSummaryText;
    private TextView standingsText;
    private TextView competitionTitle;
    private TextView competitionDescription;
    private TextView loginStatus;
    private Button loginButton;
    private GoogleSignInClient googleSignInClient;
    private final OkHttpClient client = new OkHttpClient();
    private final String apiKey = "0858398f87f44a67b5a94ed0cbe593a9";
    private final List<CompetitionItem> competitions = new ArrayList<>();
    private FixtureAdapter fixtureAdapter;
    private CompetitionAdapter competitionAdapter;
    private int selectedCompetitionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        fixturesRecycler = findViewById(R.id.fixturesRecycler);
        competitionsRecycler = findViewById(R.id.competitionsRecycler);
        fixturesSummaryText = findViewById(R.id.fixturesSummaryText);
        standingsText = findViewById(R.id.standingsText);
        competitionTitle = findViewById(R.id.competitionTitle);
        competitionDescription = findViewById(R.id.competitionDescription);
        loginStatus = findViewById(R.id.loginStatus);
        loginButton = findViewById(R.id.loginButton);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        updateSignInStatus();
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
                if (account != null) {
                    signOut();
                } else {
                    signIn();
                }
            }
        });

        competitionDescription.setText("Wähle eine Liga aus der Liste, um Spiele, Tabellen und Echtzeitinfos zu sehen.");
        fixturesRecycler.setLayoutManager(new LinearLayoutManager(this));
        fixtureAdapter = new FixtureAdapter(new ArrayList<>());
        fixturesRecycler.setAdapter(fixtureAdapter);

        competitionsRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        competitionAdapter = new CompetitionAdapter(competitions, new CompetitionAdapter.OnCompetitionSelectedListener() {
            @Override
            public void onCompetitionSelected(CompetitionItem competition) {
                selectedCompetitionId = competition.id;
                competitionAdapter.setSelectedCompetitionId(competition.id);
                updateHeader(competition);
                updateSummary("Lade Spiele für " + competition.name + "...");
                loadFixturesForCompetition(competition.id);
                loadStandingsForCompetition(competition.id);
            }
        });
        competitionsRecycler.setAdapter(competitionAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateSummary("Aktualisiere Spiele und Tabellen...");
                loadFixturesForCompetition(selectedCompetitionId);
                loadStandingsForCompetition(selectedCompetitionId);
            }
        });

        loadCompetitions();
    }

    private void updateSignInStatus() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            loginStatus.setText("Angemeldet als " + account.getDisplayName());
            loginButton.setText("Abmelden");
        } else {
            loginStatus.setText("Nicht angemeldet");
            loginButton.setText("Mit Google anmelden");
        }
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                updateSignInStatus();
                Toast.makeText(MainActivity.this, "Abgemeldet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                task.getResult(ApiException.class);
                updateSignInStatus();
                Toast.makeText(this, "Erfolgreich angemeldet", Toast.LENGTH_SHORT).show();
            } catch (ApiException e) {
                Toast.makeText(this, "Fehler bei Anmeldung: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadCompetitions() {
        swipeRefreshLayout.setRefreshing(true);
        Request request = new Request.Builder()
                .url("https://api.football-data.org/v4/competitions?plan=TIER_ONE")
                .addHeader("X-Auth-Token", apiKey)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(MainActivity.this, "Konnte Ligen nicht laden", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : null;
                if (!response.isSuccessful() || body == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                            Toast.makeText(MainActivity.this, "Ungültige Antwort vom Server", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                final List<CompetitionItem> parsed = parseCompetitions(body);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        competitions.clear();
                        competitions.addAll(parsed);
                        competitionsRecycler.getAdapter().notifyDataSetChanged();
                        if (!parsed.isEmpty()) {
                            CompetitionItem firstCompetition = parsed.get(0);
                            selectedCompetitionId = firstCompetition.id;
                            competitionAdapter.setSelectedCompetitionId(firstCompetition.id);
                            updateHeader(firstCompetition);
                            loadFixturesForCompetition(selectedCompetitionId);
                            loadStandingsForCompetition(selectedCompetitionId);
                        }
                    }
                });
            }
        });
    }

    private void loadFixturesForCompetition(int competitionId) {
        swipeRefreshLayout.setRefreshing(true);
        updateSummary("Spiele werden geladen...");
        Request request = new Request.Builder()
                .url("https://api.football-data.org/v4/competitions/" + competitionId + "/matches?status=SCHEDULED")
                .addHeader("X-Auth-Token", apiKey)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        updateSummary("Spiele konnten nicht geladen werden.");
                        Toast.makeText(MainActivity.this, "Fehler beim Laden der Spiele", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : null;
                if (!response.isSuccessful() || body == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                            updateSummary("Spiele konnten nicht geladen werden.");
                            Toast.makeText(MainActivity.this, "Ungültige Antwort vom Server", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }

                final List<Match> fixtures = parseMatches(body);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        fixtureAdapter.updateFixtures(fixtures);
                        if (fixtures.isEmpty()) {
                            updateSummary("Für diese Liga sind keine anstehenden Spiele verfügbar.");
                        } else {
                            updateSummary(fixtures.size() + " anstehende Spiele geladen.");
                        }
                    }
                });
            }
        });
    }

    private void loadStandingsForCompetition(int competitionId) {
        Request request = new Request.Builder()
                .url("https://api.football-data.org/v4/competitions/" + competitionId + "/standings")
                .addHeader("X-Auth-Token", apiKey)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        standingsText.setText("Tabellen-Daten gerade nicht verfügbar.");
                        updateSummary("Kein Tabellen-Update verfügbar.");
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : null;
                if (!response.isSuccessful() || body == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            standingsText.setText("Tabellen-Daten gerade nicht verfügbar.");
                            updateSummary("Kein Tabellen-Update verfügbar.");
                        }
                    });
                    return;
                }

                final String tablePreview = parseStandingsPreview(body);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        standingsText.setText(tablePreview);
                        updateSummary("Aktualisierte Tabelle und Spiele geladen.");
                    }
                });
            }
        });
    }

    private List<CompetitionItem> parseCompetitions(String body) {
        List<CompetitionItem> items = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(body);
            JSONArray competitionsArray = root.optJSONArray("competitions");
            if (competitionsArray == null) {
                return items;
            }
            for (int i = 0; i < competitionsArray.length(); i++) {
                JSONObject competitionJson = competitionsArray.optJSONObject(i);
                if (competitionJson == null) {
                    continue;
                }
                int id = competitionJson.optInt("id", 0);
                String name = competitionJson.optString("name", "Liga");
                String areaName = "Unbekannt";
                JSONObject areaObj = competitionJson.optJSONObject("area");
                if (areaObj != null) {
                    areaName = areaObj.optString("name", areaName);
                }
                String seasonLabel = "Saison nicht verfügbar";
                JSONObject seasonObj = competitionJson.optJSONObject("currentSeason");
                if (seasonObj != null) {
                    String startDate = seasonObj.optString("startDate", "");
                    String endDate = seasonObj.optString("endDate", "");
                    if (!startDate.isEmpty() && !endDate.isEmpty()) {
                        seasonLabel = startDate + " – " + endDate;
                    }
                }
                if (id > 0) {
                    items.add(new CompetitionItem(id, name, areaName, seasonLabel));
                }
            }
        } catch (Exception ignored) {
        }
        return items;
    }

    private List<Match> parseMatches(String body) {
        List<Match> fixtures = new ArrayList<>();
        try {
            JSONObject root = new JSONObject(body);
            JSONArray matches = root.optJSONArray("matches");
            if (matches == null) {
                return fixtures;
            }
            for (int i = 0; i < matches.length(); i++) {
                JSONObject matchJson = matches.optJSONObject(i);
                if (matchJson == null) {
                    continue;
                }
                String utcDate = matchJson.optString("utcDate", "");
                String status = matchJson.optString("status", "SCHEDULED");
                String prettyStatus = formatStatus(status);
                JSONObject competitionObj = matchJson.optJSONObject("competition");
                String competitionName = competitionObj != null ? competitionObj.optString("name", "Wettbewerb") : "Wettbewerb";
                JSONObject homeTeamObj = matchJson.optJSONObject("homeTeam");
                JSONObject awayTeamObj = matchJson.optJSONObject("awayTeam");
                String homeTeam = homeTeamObj != null ? homeTeamObj.optString("name", "Heim") : "Heim";
                String awayTeam = awayTeamObj != null ? awayTeamObj.optString("name", "Auswärts") : "Auswärts";
                fixtures.add(new Match(utcDate, competitionName, homeTeam, awayTeam, prettyStatus));
            }
        } catch (Exception ignored) {
        }
        return fixtures;
    }

    private String parseStandingsPreview(String body) {
        try {
            JSONObject root = new JSONObject(body);
            JSONArray standings = root.optJSONArray("standings");
            if (standings == null || standings.length() == 0) {
                return "Tabellen-Daten gerade nicht verfügbar.";
            }
            JSONObject firstStandings = standings.optJSONObject(0);
            JSONArray table = firstStandings.optJSONArray("table");
            if (table == null || table.length() == 0) {
                return "Tabellen-Daten gerade nicht verfügbar.";
            }
            StringBuilder preview = new StringBuilder();
            preview.append("Top 5 Tabelle:\n");
            for (int i = 0; i < Math.min(5, table.length()); i++) {
                JSONObject row = table.optJSONObject(i);
                if (row == null) {
                    continue;
                }
                JSONObject teamObj = row.optJSONObject("team");
                String teamName = teamObj != null ? teamObj.optString("name", "Team") : "Team";
                int points = row.optInt("points", 0);
                preview.append((i + 1)).append(". ").append(teamName).append(" • ").append(points).append(" pts\n");
            }
            return preview.toString().trim();
        } catch (Exception ignored) {
            return "Tabellen-Daten gerade nicht verfügbar.";
        }
    }

    private void updateHeader(CompetitionItem competition) {
        competitionTitle.setText(competition.name);
        competitionDescription.setText(competition.areaName + " • " + competition.seasonLabel);
    }

    private void updateSummary(String message) {
        fixturesSummaryText.setText(message);
    }

    static String formatDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) {
            return "TBD";
        }
        try {
            return ZonedDateTime.parse(rawDate).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        } catch (Exception e) {
            return rawDate.replace("T", " ").replace("Z", "");
        }
    }

    private String formatStatus(String status) {
        if (status == null || status.isEmpty()) {
            return "Scheduled";
        }
        switch (status.toUpperCase()) {
            case "LIVE":
                return "LIVE NOW";
            case "IN_PLAY":
                return "IN PLAY";
            case "PAUSED":
                return "HALF TIME";
            case "FINISHED":
                return "FULL TIME";
            case "SCHEDULED":
            default:
                return "SCHEDULED";
        }
    }

    private static class FixtureAdapter extends RecyclerView.Adapter<FixtureAdapter.FixtureViewHolder> {
        private final List<Match> fixtures;

        FixtureAdapter(List<Match> fixtures) {
            this.fixtures = fixtures;
        }

        void updateFixtures(List<Match> newFixtures) {
            fixtures.clear();
            fixtures.addAll(newFixtures);
            notifyDataSetChanged();
        }

        @Override
        public FixtureViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fixture, parent, false);
            return new FixtureViewHolder(view);
        }

        @Override
        public void onBindViewHolder(FixtureViewHolder holder, int position) {
            holder.bind(fixtures.get(position));
        }

        @Override
        public int getItemCount() {
            return fixtures.size();
        }

        static class FixtureViewHolder extends RecyclerView.ViewHolder {
            private final TextView titleView;
            private final TextView dateView;
            private final TextView leagueView;
            private final TextView statusView;

            FixtureViewHolder(View itemView) {
                super(itemView);
                titleView = itemView.findViewById(R.id.fixtureTitle);
                dateView = itemView.findViewById(R.id.fixtureDate);
                leagueView = itemView.findViewById(R.id.fixtureLeague);
                statusView = itemView.findViewById(R.id.fixtureStatus);
            }

            void bind(Match match) {
                titleView.setText(match.homeTeam + " vs " + match.awayTeam);
                dateView.setText(formatDate(match.utcDate));
                leagueView.setText(match.competitionName);
                statusView.setText(match.status);
            }
        }
    }

    private static class CompetitionAdapter extends RecyclerView.Adapter<CompetitionAdapter.CompetitionViewHolder> {
        private final List<CompetitionItem> competitions;
        private final OnCompetitionSelectedListener listener;

        CompetitionAdapter(List<CompetitionItem> competitions, OnCompetitionSelectedListener listener) {
            this.competitions = competitions;
            this.listener = listener;
        }

        private int selectedCompetitionId = -1;

        @Override
        public CompetitionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_competition, parent, false);
            return new CompetitionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CompetitionViewHolder holder, int position) {
            CompetitionItem competition = competitions.get(position);
            boolean isSelected = competition.id == selectedCompetitionId;
            holder.bind(competition, isSelected, listener);
        }

        @Override
        public int getItemCount() {
            return competitions.size();
        }

        void setSelectedCompetitionId(int selectedCompetitionId) {
            this.selectedCompetitionId = selectedCompetitionId;
            notifyDataSetChanged();
        }

        interface OnCompetitionSelectedListener {
            void onCompetitionSelected(CompetitionItem competition);
        }

        static class CompetitionViewHolder extends RecyclerView.ViewHolder {
            private final CardView cardView;
            private final TextView nameView;
            private final TextView areaView;

            CompetitionViewHolder(View itemView) {
                super(itemView);
                cardView = (CardView) itemView;
                nameView = itemView.findViewById(R.id.competitionName);
                areaView = itemView.findViewById(R.id.competitionArea);
            }

            void bind(final CompetitionItem competition, boolean selected, final OnCompetitionSelectedListener listener) {
                nameView.setText(competition.name);
                areaView.setText(competition.areaName);
                int cardColor = ContextCompat.getColor(itemView.getContext(), selected ? R.color.brand_green : R.color.surface);
                cardView.setCardBackgroundColor(cardColor);
                nameView.setTextColor(ContextCompat.getColor(itemView.getContext(), selected ? R.color.white : R.color.text_primary));
                areaView.setTextColor(ContextCompat.getColor(itemView.getContext(), selected ? R.color.white : R.color.text_secondary));
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onCompetitionSelected(competition);
                    }
                });
            }
        }
    }

    private static class Match {
        final String utcDate;
        final String competitionName;
        final String homeTeam;
        final String awayTeam;
        final String status;

        Match(String utcDate, String competitionName, String homeTeam, String awayTeam, String status) {
            this.utcDate = utcDate;
            this.competitionName = competitionName;
            this.homeTeam = homeTeam;
            this.awayTeam = awayTeam;
            this.status = status;
        }
    }

    private static class CompetitionItem {
        final int id;
        final String name;
        final String areaName;
        final String seasonLabel;

        CompetitionItem(int id, String name, String areaName, String seasonLabel) {
            this.id = id;
            this.name = name;
            this.areaName = areaName;
            this.seasonLabel = seasonLabel;
        }
    }
}
