package com.example.playerapp.network

import android.util.Base64
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

object SpotifyAuth {
    private const val CLIENT_ID = "9912a037a40f4392829a8d0a487b5a72"
    private const val CLIENT_SECRET = "b438d72b8c1e42efb601a287baccf18"

    suspend fun fetchToken(): String = suspendCancellableCoroutine { cont ->
        val creds = "$CLIENT_ID:$CLIENT_SECRET"
        val basic = Base64.encodeToString(creds.toByteArray(), Base64.NO_WRAP)

        val requestBody = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .build()

        val request = Request.Builder()
            .url("https://accounts.spotify.com/api/token")
            .post(requestBody)
            .addHeader("Authorization", "Basic $basic")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                cont.cancel(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val body = it.body?.string()
                    if (body != null) {
                        val json = JSONObject(body)
                        val token = json.getString("access_token")
                        cont.resume(token, null)
                    } else {
                        cont.cancel(IllegalStateException("Empty body"))
                    }
                }
            }
        })
    }
}
