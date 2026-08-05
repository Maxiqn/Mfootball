package com.maxiqn.mfootball

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var fixturesRecycler: RecyclerView
    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val competitionId = 2021 // Premier League, as an example
    private val apiKey = "YOUR_FOOTBALL_DATA_API_KEY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        swipeRefreshLayout = findViewById(R.id.swipeRefresh)
        fixturesRecycler = findViewById(R.id.fixturesRecycler)
        fixturesRecycler.layoutManager = LinearLayoutManager(this)

        swipeRefreshLayout.setOnRefreshListener { loadFixtures() }
        loadFixtures()
    }

    private fun loadFixtures() {
        swipeRefreshLayout.isRefreshing = true
        val request = Request.Builder()
            .url("https://api.football-data.org/v4/competitions/$competitionId/matches?status=SCHEDULED")
            .addHeader("X-Auth-Token", apiKey)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(this@MainActivity, "Fehler beim Laden der Spiele", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (!response.isSuccessful || body == null) {
                    runOnUiThread {
                        swipeRefreshLayout.isRefreshing = false
                        Toast.makeText(this@MainActivity, "Ungültige Antwort vom Server", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                val adapter: JsonAdapter<MatchesResponse> = moshi.adapter(MatchesResponse::class.java)
                val matchesResponse = adapter.fromJson(body)
                runOnUiThread {
                    swipeRefreshLayout.isRefreshing = false
                    if (matchesResponse == null) {
                        Toast.makeText(this@MainActivity, "Daten konnten nicht verarbeitet werden", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }
                    fixturesRecycler.adapter = FixtureAdapter(matchesResponse.matches)
                }
            }
        })
    }

    private class FixtureAdapter(private val fixtures: List<Match>) : RecyclerView.Adapter<FixtureAdapter.FixtureViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FixtureViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fixture, parent, false)
            return FixtureViewHolder(view)
        }

        override fun onBindViewHolder(holder: FixtureViewHolder, position: Int) {
            holder.bind(fixtures[position])
        }

        override fun getItemCount(): Int = fixtures.size

        class FixtureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val titleView: TextView = itemView.findViewById(R.id.fixtureTitle)
            private val dateView: TextView = itemView.findViewById(R.id.fixtureDate)
            private val leagueView: TextView = itemView.findViewById(R.id.fixtureLeague)

            fun bind(match: Match) {
                titleView.text = "${match.homeTeam.name} vs ${match.awayTeam.name}"
                dateView.text = try {
                    ZonedDateTime.parse(match.utcDate).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                } catch (e: Exception) {
                    match.utcDate
                }
                leagueView.text = "${match.competition?.name ?: "Wettbewerb"}"
            }
        }
    }
}

data class MatchesResponse(val matches: List<Match>)

data class Match(
    val utcDate: String,
    val competition: Competition?,
    val homeTeam: Team,
    val awayTeam: Team
)

data class Competition(val name: String)

data class Team(val name: String)
