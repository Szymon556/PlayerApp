package com.example.playerapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.playerapp.network.SpotifyAuthManager

class LogoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout)

        val statusText = findViewById<TextView>(R.id.logoutStatus)
        statusText.text = "Wylogowywanie..."

        // Wykonujemy wylogowanie (czyści tokeny i otwiera stronę logout Spotify)
        SpotifyAuthManager.logout(this)

        // Poczekaj 3 sekundy, a potem przejdź do LoginActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }, 3000)
    }
}
