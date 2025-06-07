package com.example.playerapp

import android.content.Intent
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
import com.example.playerapp.firebase.FirebaseRealtimeRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.ktx.database
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var trackList: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var userNameView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)

        trackList = findViewById(R.id.trackList)
        searchInput = findViewById(R.id.searchInput)
        searchButton = findViewById(R.id.searchButton)
        userNameView = findViewById(R.id.userNameView)

        trackList.layoutManager = LinearLayoutManager(this)

        // ✅ Zaloguj użytkownika do Firebase, jeśli jeszcze nie jest zalogowany
        if (Firebase.auth.currentUser == null) {
            Firebase.auth.signInAnonymously()
                .addOnSuccessListener {
                    loadAndDisplayUser()
                }
                .addOnFailureListener {
                    userNameView.text = "Błąd logowania do Firebase"
                }
        } else {
            loadAndDisplayUser()
        }

        // 🔍 Obsługa przycisku "Szukaj"
        searchButton.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isNotBlank()) {
                lifecycleScope.launch {
                    try {
                        val results: List<Track> = SpotifyApi.searchTrack(this@MainActivity, query)
                        trackList.adapter = TrackAdapter(results)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Możesz tu dodać Toast.makeText(...)
                    }
                }
            }
        }

        // 🚪 Wylogowanie
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            SpotifyAuthManager.logout(this)
            startActivity(Intent(this, StartupActivity::class.java))
            finish()
        }
    }

    // 🔄 Pobierz dane użytkownika z Firebase i wyświetl
    private fun loadAndDisplayUser() {
        lifecycleScope.launch {
            val user: SpotifyUser? = SpotifyApi.getCurrentUser(this@MainActivity)
            if (user != null) {
                FirebaseRealtimeRepository.getUser(user.id) { firebaseUser ->
                    if (firebaseUser != null) {
                        userNameView.text = "Zalogowany jako: ${firebaseUser.displayName}"
                    } else {
                        userNameView.text = "Zalogowany jako: ${user.displayName}"
                    }
                }
            } else {
                userNameView.text = "Nieznany użytkownik"
            }
        }
    }
}
