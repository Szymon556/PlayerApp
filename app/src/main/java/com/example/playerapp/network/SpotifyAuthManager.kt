package com.example.playerapp.network

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import androidx.core.content.edit
import com.example.playerapp.LogoutActivity

object SpotifyAuthManager {
    private const val CLIENT_ID = "9912a037a40f4392829a8d0a487b5a72"
    private const val CLIENT_SECRET = "b438d72b8c1e42efbe601a287baccf18"
    private const val REDIRECT_URI = "myapp://callback"
    private const val TOKEN_URL = "https://accounts.spotify.com/api/token"

    private const val PREFS_NAME = "SpotifyPrefs"
    private const val ACCESS_TOKEN = "access_token"
    private const val REFRESH_TOKEN = "refresh_token"
    private const val EXPIRES_AT = "expires_at"

    fun getLoginUri(): Uri = Uri.Builder()
        .scheme("https")
        .authority("accounts.spotify.com")
        .appendPath("authorize")
        .appendQueryParameter("client_id", CLIENT_ID)
        .appendQueryParameter("response_type", "code")
        .appendQueryParameter("redirect_uri", REDIRECT_URI)
        .appendQueryParameter("scope", "user-library-modify user-library-read")
        .build()

    suspend fun exchangeCodeForToken(context: Context, code: String): Boolean = withContext(Dispatchers.IO) {
        val creds = "$CLIENT_ID:$CLIENT_SECRET"
        val basic = Base64.encodeToString(creds.toByteArray(), Base64.NO_WRAP)

        val body = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", REDIRECT_URI)
            .build()

        val request = Request.Builder()
            .url(TOKEN_URL)
            .post(body)
            .addHeader("Authorization", "Basic $basic")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()

        val response = OkHttpClient().newCall(request).execute()
        val responseBody = response.body?.string() ?: return@withContext false
        val json = JSONObject(responseBody)
        Log.d("SpotifyAuth", "Token exchange response: $json")

        if (!json.has("access_token")) return@withContext false

        val accessToken = json.getString("access_token")
        val refreshToken = json.optString("refresh_token", "")
        val expiresIn = json.optInt("expires_in", 3600)

        saveTokens(context, accessToken, refreshToken, expiresIn)
        return@withContext true
    }

    suspend fun refreshTokenIfNeeded(context: Context) = withContext(Dispatchers.IO) {
        val prefs = getPrefs(context)
        if (prefs.getBoolean("is_logged_out", false)) {
            Log.d("SpotifyAuth", "Użytkownik jest oznaczony jako wylogowany — pomijam odświeżenie tokena.")
            return@withContext
        }
        val refreshToken = prefs.getString(REFRESH_TOKEN, null) ?: return@withContext

        val creds = "$CLIENT_ID:$CLIENT_SECRET"
        val basic = Base64.encodeToString(creds.toByteArray(), Base64.NO_WRAP)

        val body = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("refresh_token", refreshToken)
            .build()

        val request = Request.Builder()
            .url(TOKEN_URL)
            .post(body)
            .addHeader("Authorization", "Basic $basic")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()

        val response = OkHttpClient().newCall(request).execute()
        val responseBody = response.body?.string() ?: return@withContext
        val json = JSONObject(responseBody)
        Log.d("SpotifyAuth", "Refresh response: $json")

        if (json.has("access_token")) {
            val accessToken = json.getString("access_token")
            val expiresIn = json.optInt("expires_in", 3600)
            saveTokens(context, accessToken, refreshToken, expiresIn)
        }
    }

    fun getAccessToken(context: Context): String? {
        val prefs = getPrefs(context)
        val expiresAt = prefs.getLong(EXPIRES_AT, 0)
        val currentTime = System.currentTimeMillis()

        val token = prefs.getString(ACCESS_TOKEN, null)
        Log.d("SpotifyAuth", "Current access token: $token")
        Log.d("SpotifyAuth", "Token expires at: $expiresAt, current time: $currentTime")

        return if (currentTime < expiresAt) {
            token
        } else {
            Log.d("SpotifyAuth", "Access token has expired.")
            null
        }
    }


    fun isLoggedIn(context: Context): Boolean {
        val prefs = getPrefs(context)
        val token = prefs.getString("access_token", null)
        val expiresAt = prefs.getLong("expires_at", 0)
        val now = System.currentTimeMillis()

        val isValid = !token.isNullOrBlank() && now < expiresAt

        Log.d("SpotifyAuth", "isLoggedIn = $isValid, token=$token, expiresAt=$expiresAt, now=$now")

        return isValid
    }


    fun logout(context: Context) {
        val prefs = getPrefs(context)

        prefs.edit {
            remove(ACCESS_TOKEN)
            remove(REFRESH_TOKEN)
            remove(EXPIRES_AT)
            putBoolean("is_logged_out", true)
        }

        Log.d("SpotifyAuth", "Tokeny usunięte, oznaczono jako wylogowany")

        // Przekierowanie do ekranu LogoutActivity, który otworzy stronę logout i pokaże komunikat
        val intent = Intent(context, LogoutActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }




    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun saveTokens(context: Context, accessToken: String, refreshToken: String, expiresIn: Int) {
        val prefs = getPrefs(context) // <-- to ważna zmiana!
        prefs.edit {
            putString(ACCESS_TOKEN, accessToken)
            putString(REFRESH_TOKEN, refreshToken)
            putLong("expires_at", System.currentTimeMillis() + expiresIn * 1000)
        }
        Log.d("SpotifyAuth", "Saved access token: $accessToken")
        Log.d("SpotifyAuth", "Saved expires_at: ${System.currentTimeMillis() + expiresIn * 1000}")
    }

}
