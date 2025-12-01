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

    suspend fun retrieveHistoricalEvents(location: LatLng, radius: Int): List<HistoricalEvent> {

        val query: String = """
        SELECT DISTINCT ?event ?eventLabel ?location ?dist ?time ?article WHERE {
            # Berlin coordinates
            wd:Q64 wdt:P625 ?berlinLoc .
            SERVICE wikibase:around {
                ?event wdt:P625 ?location .
                bd:serviceParam wikibase:center "Point(${location.longitude} ${location.latitude})"^^geo:wktLiteral ;
                                wikibase:radius "$radius" ;
                                wikibase:distance ?dist .
            }
            ?event wdt:P585 ?time.
  
            FILTER EXISTS {
                VALUES ?type {
                wd:Q1190554    # event
                wd:Q198        # historical event
                wd:Q1656682    # significant event
                wd:Q839954     # heritage site
                wd:Q11707      # archaeological site
                wd:Q575759     # historic site
                wd:Q9259       # monument
                wd:Q570116     # UNESCO World Heritage Site
                }
                ?event wdt:P31/wdt:P279* ?type .
            }
            
            OPTIONAL {
                ?article schema:about ?event .
                ?article schema:isPartOf <https://en.wikipedia.org/> .
            }
  
            SERVICE wikibase:label {
                bd:serviceParam wikibase:language "en" .
            }
        } ORDER BY ASC(?time)
        LIMIT 50
        """.trimIndent()

        /*
                    SELECT DISTINCT ?event ?eventLabel ?location ?distance ?time WHERE {
              SERVICE wikibase:around {
                ?event wdt:P625 ?location .
                bd:serviceParam wikibase:center "Point(${location.longitude} ${location.latitude})"^^geo:wktLiteral .
                bd:serviceParam wikibase:radius "$radius" .
                bd:serviceParam wikibase:distance ?distance .
              }


              SERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE],en". }
            }
            ORDER BY ASC(?distance)
            LIMIT 500

         */

        /*
        SELECT DISTINCT ?event ?eventLabel ?location ?dist ?time WHERE {
  # Berlin coordinates
  wd:Q64 wdt:P625 ?berlinLoc .
  SERVICE wikibase:around {
    ?event wdt:P625 ?location .
    bd:serviceParam wikibase:center ?berlinLoc ;
                    wikibase:radius "100" ;
                    wikibase:distance ?dist .
  }
  ?event wdt:P585 ?time.

    FILTER EXISTS {
    VALUES ?type {
      wd:Q1190554    # event
      wd:Q198        # historical event
      wd:Q1656682    # significant event
      wd:Q839954     # heritage site
      wd:Q11707      # archaeological site
      wd:Q575759     # historic site
      wd:Q9259       # monument
      wd:Q570116     # UNESCO World Heritage Site
    }
    ?event wdt:P31/wdt:P279* ?type .
  }

  SERVICE wikibase:label {
    bd:serviceParam wikibase:language "en" .
  }
} ORDER BY ASC(?dist)
LIMIT 500

         */

        /*
                      # Broader categories
              VALUES ?class {
                wd:Q839954      # historic site
                wd:Q271669      # archaeological site
                wd:Q35509       # conflict / battle
                wd:Q57821       # monument
                wd:Q4989906     # heritage site
                wd:Q570116      # historic district
              }

              ?event wdt:P31/wdt:P279* ?class .

         */

        /*
       SELECT ?place ?placeLabel ?location ?dist ?start ?end ?time WHERE {
  # Berlin coordinates
  wd:Q64 wdt:P625 ?berlinLoc .
  SERVICE wikibase:around {
    ?place wdt:P625 ?location .
    bd:serviceParam wikibase:center ?berlinLoc ;
                    wikibase:radius "100" ;
                    wikibase:distance ?dist .
  }
  # Is an airport
  OPTIONAL { ?place wdt:P580 ?start. }
  OPTIONAL { ?place wdt:P582 ?end. }
  OPTIONAL { ?place wdt:P585 ?time. }
  SERVICE wikibase:label {
    bd:serviceParam wikibase:language "en" .
  }
} ORDER BY ASC(?dist)
LIMIT 100
         */


        /*
        SELECT DISTINCT ?place ?placeLabel ?location ?dist ?start ?end ?time WHERE {
  # Berlin coordinates
  wd:Q64 wdt:P625 ?berlinLoc .
  SERVICE wikibase:around {
    ?place wdt:P625 ?location .
    bd:serviceParam wikibase:center ?berlinLoc ;
                    wikibase:radius "100" ;
                    wikibase:distance ?dist .
  }
  # Is an airport
  OPTIONAL { ?place wdt:P580 ?start. }
  OPTIONAL { ?place wdt:P582 ?end. }
  ?place wdt:P585 ?time.
  SERVICE wikibase:label {
    bd:serviceParam wikibase:language "en" .
  }
} ORDER BY ASC(?dist)
LIMIT 100

         */

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
                    val event = HistoricalEvent(
                        name = currentEvent.getJSONObject("eventLabel").getString("value"),
                        date = formatDate(currentEvent.getJSONObject("time").getString("value")),
                        location = getLatLngFromPoint(currentEvent.getJSONObject("location").getString("value")),
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