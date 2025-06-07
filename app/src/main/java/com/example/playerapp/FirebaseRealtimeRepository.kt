package com.example.playerapp.firebase

import com.example.playerapp.model.SpotifyUser
import com.example.playerapp.model.Track
import com.google.firebase.database.FirebaseDatabase

object FirebaseRealtimeRepository {
    private val db = FirebaseDatabase.getInstance().reference

    fun saveUser(user: SpotifyUser) {
        db.child("users").child(user.id).setValue(user)
    }

    fun getUser(userId: String, callback: (SpotifyUser?) -> Unit) {
        db.child("users").child(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(SpotifyUser::class.java)
                callback(user)
            }
            .addOnFailureListener {
                callback(null)
            }
    }


    fun saveOrUpdateTrack(trackId: String, track: Track) {
        val songRef = db.child("songs").child(trackId)
        songRef.get().addOnSuccessListener { snapshot ->
            val currentVotes = snapshot.child("votes").getValue(Int::class.java) ?: 0
            val updatedTrack = track.copy(votes = currentVotes)
            songRef.setValue(updatedTrack)
        }
    }

    fun upvoteTrack(trackId: String) {
        val songRef = db.child("songs").child(trackId).child("votes")
        songRef.get().addOnSuccessListener { snapshot ->
            val currentVotes = snapshot.getValue(Int::class.java) ?: 0
            songRef.setValue(currentVotes + 1)
        }
    }
}
