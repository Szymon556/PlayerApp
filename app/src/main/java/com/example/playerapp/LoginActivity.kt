package com.example.playerapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.playerapp.network.SpotifyAuthManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        statusText = findViewById(R.id.statusText) // <- To dodaj!
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            val authUri = SpotifyAuthManager.getLoginUri()
            val intent = Intent(Intent.ACTION_VIEW, authUri)
            startActivity(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val uri = intent?.data
        if (uri != null && uri.toString().startsWith("myapp://callback")) {
            val code = uri.getQueryParameter("code")
            if (code != null) {
                val prefs = getSharedPreferences("SpotifyPrefs", MODE_PRIVATE)
                val wasLoggedOut = prefs.getBoolean("is_logged_out", false)

                if (wasLoggedOut) {
                    lifecycleScope.launch {
                        statusText.visibility = View.VISIBLE // <- teraz działa

                        val success = SpotifyAuthManager.exchangeCodeForToken(applicationContext, code)
                        if (success) {
                            prefs.edit().remove("is_logged_out").apply()
                            val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
                            mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(mainIntent)
                            finish()
                        }
                    }
                } else {
                    startActivity(Intent(this, StartupActivity::class.java))
                    finish()
                }
            }
        }
    }
}