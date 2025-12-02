package com.example.project2historyapp

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import org.json.JSONObject
import java.net.URLEncoder
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.SignStyle
import java.time.temporal.ChronoField
import java.util.concurrent.TimeUnit

object QueryManager {
    val client: OkHttpClient

    init {
        val builder = OkHttpClient.Builder()
            .connectTimeout(240, TimeUnit.SECONDS)
            .readTimeout(240, TimeUnit.SECONDS)
            .writeTimeout(240, TimeUnit.SECONDS)
        val loggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(loggingInterceptor)
        client = builder.build()
    }

    suspend fun retrieveHistoricalEvents(location: LatLng, startTime: String, endTime: String, radius: Int): List<HistoricalEvent> {

        // Query created after much pain playing around with https://query.wikidata.org/ and looking at the examples given
        val query: String = """
        SELECT DISTINCT ?event ?eventLabel ?location ?dist ?time ?article WHERE {
        SERVICE wikibase:around {
            ?event wdt:P625 ?location .
            bd:serviceParam wikibase:center "Point(${location.longitude} ${location.latitude})"^^geo:wktLiteral ;
                            wikibase:radius "${radius}" ;
                            wikibase:distance ?dist .
        }

        OPTIONAL { ?event wdt:P585 ?p585 . }
        OPTIONAL { ?event wdt:P580 ?p580 . }
        OPTIONAL { ?event wdt:P582 ?p582 . }

        BIND(COALESCE(?p585, ?p580, ?p582) AS ?time)
        FILTER(BOUND(?time))

        FILTER(?time >= "${startTime}"^^xsd:dateTime &&
               ?time <= "${endTime}"^^xsd:dateTime)

        FILTER EXISTS {
          VALUES ?type {
            wd:Q1190554 wd:Q198 wd:Q1656682 wd:Q839954
            wd:Q11707 wd:Q575759 wd:Q9259 wd:Q570116
          }
          ?event wdt:P31/wdt:P279* ?type .
        }

        OPTIONAL {
          ?article schema:about ?event .
          ?article schema:isPartOf <https://en.wikipedia.org/> .
        }

        SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
        }
        ORDER BY ASC(?time)
        LIMIT 50
        """.trimIndent()

        val request = Request.Builder()
            .url("https://query.wikidata.org/sparql?format=json&query=${URLEncoder.encode(query, "UTF-8")}")
            .get()
            .build()

        try {
            val response: Response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                val eventList = mutableListOf<HistoricalEvent>()
                val json = JSONObject(responseBody)
                val events = json.getJSONObject("results").getJSONArray("bindings")
                for (i in 0 until events.length()) {
                    val currentEvent = events.getJSONObject(i)

                    var article: String? = null
                    try {
                        val articleObject = currentEvent.getJSONObject("article")
                        article = articleObject.getString("value")
                    } catch(error: JSONException) {

                    }

                    val location = getLatLngFromPoint(currentEvent.getJSONObject("location").getString("value"))
                    val event = HistoricalEvent(
                        name = currentEvent.getJSONObject("eventLabel").getString("value"),
                        date = formatDate(currentEvent.getJSONObject("time").getString("value")),
                        latitude = location.latitude,
                        longitude = location.longitude,
                        article = article
                    )

                    eventList.add(event)
                }

                response.close()
                return eventList
            } else {
                response.close()
                return listOf()
            }

        } catch(error: Error) {
            Log.d("ERROR", error.message.toString())
            return listOf()

        }
    }

    fun getLatLngFromPoint(point: String): LatLng {
        val latLng = point.removePrefix("Point(").removeSuffix(")")
        val (lonStr, latStr) = latLng.split(" ")
        val lat = latStr.toDouble()
        val lon = lonStr.toDouble()
        return LatLng(lat, lon)
    }

    // Convert date from WikiData into date that can be displayed without hurting eyes
    fun formatDate(date: String): String {
        val parser = DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 1, 10, SignStyle.NORMAL)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral('T')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .appendOffset("+HH:MM", "Z")
            .toFormatter()
        val localDate = OffsetDateTime.parse(date, parser).toLocalDate()

        val outputFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy")
        return localDate.format(outputFormat)
    }

}