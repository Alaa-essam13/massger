package com.example.massger.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.database.database
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val firebaseDatabase = Firebase.database
    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels = _channels.asStateFlow()

    init {
        getChannels()
    }

    private fun getChannels() {
        firebaseDatabase.getReference("channels").get()
            .addOnSuccessListener { dataSnapshot ->
                val list = mutableListOf<Channel>()
                dataSnapshot.children.forEach { data ->
                    val channel =
                        Channel(data.key!!, data.child("name").getValue(String::class.java) ?: "")
                    list.add(channel)
                }
                _channels.value = list
                Log.d("TAG", "Retrieved ${list.size} channels")
            }
            .addOnFailureListener { exception ->
                Log.e("TAG", "Error getting channels", exception)
            }
    }

    fun addChannel(name: String) {
        val key = firebaseDatabase.getReference("channels").push().key
        if (key != null) {
            firebaseDatabase.getReference("channels").child(key).setValue(Channel(key, name))
                .addOnSuccessListener {
                    getChannels()
                    Log.d("TAG", "Channel '$name' added successfully")
                }
                .addOnFailureListener { exception ->
                    Log.e("TAG", "Error adding channel", exception)
                }
        } else {
            Log.e("TAG", "Failed to generate key for channel")
        }
    }

}