//  spotifyApi.kt  – tylko metoda searchTrack zmieniona
package com.example.playerapp.network

import com.example.playerapp.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import android.util.Base64

object SpotifyApi {
    private const val clientId = "9912a037a40f4392829a8d0a487b5a72"
    private const val clientSecret = "b438d72b8c1e42efbe601a287baccf18"
    private var accessToken: String? = null

    private suspend fun ensureToken() {
        if (accessToken != null) return
        withContext(Dispatchers.IO) {
            val creds = "$clientId:$clientSecret"
            val basic = Base64.encodeToString(creds.toByteArray(), Base64.NO_WRAP)
            val body = FormBody.Builder().add("grant_type", "client_credentials").build()
            val req = Request.Builder()
                .url("https://accounts.spotify.com/api/token")
                .post(body)
                .addHeader("Authorization", "Basic $basic")
                .build()
            val res = OkHttpClient().newCall(req).execute()
            val json = JSONObject(res.body!!.string())
            accessToken = json.getString("access_token")
        }
    }

    suspend fun searchTrack(query: String): List<Track> = withContext(Dispatchers.IO) {
        ensureToken()
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
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        val res = OkHttpClient().newCall(req).execute()
        if (!res.isSuccessful) return@withContext emptyList<Track>()

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
