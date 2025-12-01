package com.example.project2historyapp

import java.io.Serializable

data class LocationVisitCount(
    val name: String = "",
    var count: Long = 0
): Serializable
