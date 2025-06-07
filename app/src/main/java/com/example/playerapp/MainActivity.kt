package com.example.playerapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playerapp.adapter.TrackAdapter
import com.example.playerapp.model.Track
import com.example.playerapp.model.SpotifyUser
import com.example.playerapp.network.SpotifyApi
import com.example.playerapp.network.SpotifyAuthManager
import kotlinx.coroutines.launch
import android.content.Intent


class MainActivity : ComponentActivity() {

    private lateinit var trackList: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var userNameView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchInput = findViewById(R.id.searchInput)
        searchButton = findViewById(R.id.searchButton)
        trackList = findViewById(R.id.trackList)
        userNameView = findViewById(R.id.userNameView)

        trackList.layoutManager = LinearLayoutManager(this)

        // Pobierz dane użytkownika
        lifecycleScope.launch {
            val user: SpotifyUser? = SpotifyApi.getCurrentUser(this@MainActivity)
            userNameView.text = user?.displayName?.let { "Zalogowany jako: $it" } ?: "Nieznany użytkownik"
        }

        searchButton.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isNotBlank()) {
                lifecycleScope.launch {
                    try {
                        val results: List<Track> = SpotifyApi.searchTrack(this@MainActivity, query)
                        trackList.adapter = TrackAdapter(results)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Możesz dodać Toast z błędem
                    }
                }
            }
        }

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            SpotifyAuthManager.logout(this)
            startActivity(Intent(this, StartupActivity::class.java))
            finish()
        }
    }
}
