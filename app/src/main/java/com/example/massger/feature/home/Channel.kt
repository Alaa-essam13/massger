package com.example.massger.feature.home

data class Channel(
    val id: String,
    val name: String,
    val createdAt:Long = System.currentTimeMillis()
)
