package com.example.playerapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.playerapp.network.SpotifyAuthManager
import kotlinx.coroutines.launch

class StartupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val prefs = getSharedPreferences("SpotifyPrefs", MODE_PRIVATE)

            val isLoggedOut = prefs.getBoolean("is_logged_out", false)

            if (!isLoggedOut) {
                // Tylko jeśli nie był wylogowany, próbuj odświeżyć token
                SpotifyAuthManager.refreshTokenIfNeeded(this@StartupActivity)
            }

            val nextIntent = if (!isLoggedOut && SpotifyAuthManager.isLoggedIn(this@StartupActivity)) {
                Intent(this@StartupActivity, MainActivity::class.java)
            } else {
                Intent(this@StartupActivity, LoginActivity::class.java)
            }

            nextIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(nextIntent)
            finish()
        }
    }
}
