package com.example.playerapp.model

data class SpotifyUser(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val votedTracks: List<String> = emptyList()
)
