package com.example.project2historyapp

import com.google.android.gms.maps.model.LatLng

data class HistoricalEvent(
    val name: String = "",
    val date: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val article: String? = null
)
