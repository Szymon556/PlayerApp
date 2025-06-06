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
}