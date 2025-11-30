package com.example.project2historyapp

import com.google.android.gms.maps.model.LatLng

data class HistoricalEvent(
    val name: String = "",
    val date: String = "",
    val location: LatLng = LatLng(0.0, 0.0),
    val article: String? = null
)
