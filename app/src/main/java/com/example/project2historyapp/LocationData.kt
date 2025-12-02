package com.example.project2historyapp

import java.io.Serializable

data class LocationData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val start: Long = 0,
    val end: Long = 0,
    val name: String = "",
    val country: String = "",
    val dbKey: String = ""
): Serializable
