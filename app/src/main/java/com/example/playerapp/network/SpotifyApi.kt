// Rozszerzenie pliku SpotifyApi.kt o metodę do pobrania informacji o użytkowniku
package com.example.playerapp.network

import android.content.Context
import android.util.Log
import com.example.playerapp.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.example.playerapp.model.SpotifyUser


object SpotifyApi {

    suspend fun searchTrack(context: Context, query: String): List<Track> = withContext(Dispatchers.IO) {
        SpotifyAuthManager.refreshTokenIfNeeded(context)
        val token = SpotifyAuthManager.getAccessToken(context)
        if (token.isNullOrEmpty()) {
            Log.e("SpotifyApi", "No access token available")
            return@withContext emptyList<Track>()
        }

        val url = HttpUrl.Builder()
            .scheme("https")
            .host("api.spotify.com")
            .addPathSegments("v1/search")
            .addQueryParameter("q", query)
            .addQueryParameter("type", "track")
            .addQueryParameter("limit", "10")
            .build()

        val req = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .build()

        val res = OkHttpClient().newCall(req).execute()
        if (!res.isSuccessful) {
            Log.e("SpotifyApi", "API request failed: ${res.code}")
            return@withContext emptyList<Track>()
        }

        val arr = JSONObject(res.body!!.string())
            .getJSONObject("tracks")
            .getJSONArray("items")

        List(arr.length()) { i ->
            val item = arr.getJSONObject(i)
            val title = item.getString("name")
            val artist = item.getJSONArray("artists")
                .getJSONObject(0).getString("name")
            Track(title, artist)
        }
    }

    suspend fun getCurrentUser(context: Context): SpotifyUser? = withContext(Dispatchers.IO) {
        SpotifyAuthManager.refreshTokenIfNeeded(context)
        val token = SpotifyAuthManager.getAccessToken(context)
        if (token.isNullOrEmpty()) {
            Log.e("SpotifyApi", "No access token available")
            return@withContext null
        }

        val req = Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .addHeader("Authorization", "Bearer $token")
            .build()

        val res = OkHttpClient().newCall(req).execute()
        if (!res.isSuccessful) {
            Log.e("SpotifyApi", "Failed to fetch user info: ${res.code}")
            return@withContext null
        }

        val json = JSONObject(res.body!!.string())
        SpotifyUser(
            id = json.getString("id"),
            displayName = json.optString("display_name", "Unknown"),
            email = json.optString("email", "")
        )
    }
}
