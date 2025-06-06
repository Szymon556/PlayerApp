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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var trackList: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchInput = findViewById(R.id.searchInput)
        searchButton = findViewById(R.id.searchButton)
        trackList = findViewById(R.id.trackList)
        trackList.layoutManager = LinearLayoutManager(this)

        searchButton.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isNotBlank()) {
                lifecycleScope.launch {
                    val results: List<Track> = SpotifyApi.searchTrack(query)
                    trackList.adapter = TrackAdapter(results)
                }
            }
        }
    }
}
