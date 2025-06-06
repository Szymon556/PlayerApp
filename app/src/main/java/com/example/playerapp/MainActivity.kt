package com.example.playerapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playerapp.adapter.TrackAdapter
import com.example.playerapp.model.Track
import com.example.playerapp.network.SpotifyApi
import com.example.playerapp.network.SpotifyAuthManager
import kotlinx.coroutines.launch
import android.content.Intent

class MainActivity : ComponentActivity() {

    private lateinit var trackList: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicjalizacja widoków
        searchInput = findViewById(R.id.searchInput)
        searchButton = findViewById(R.id.searchButton)
        logoutButton = findViewById(R.id.logoutButton)
        trackList = findViewById(R.id.trackList)
        trackList.layoutManager = LinearLayoutManager(this)

        // Obsługa wyszukiwania
        searchButton.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isNotBlank()) {
                lifecycleScope.launch {
                    try {
                        val results: List<Track> = SpotifyApi.searchTrack(this@MainActivity, query)
                        trackList.adapter = TrackAdapter(results)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // np. Toast.makeText(this@MainActivity, "Błąd podczas wyszukiwania", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Obsługa wylogowania
        logoutButton.setOnClickListener {
            SpotifyAuthManager.logout(this)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
