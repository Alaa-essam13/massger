package com.example.massger

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Chatty:Application() {
    init {
        application =this
    }

    companion object{
        private lateinit var application: Chatty

        fun getApplicationContext(): Context = application.applicationContext
    }
}