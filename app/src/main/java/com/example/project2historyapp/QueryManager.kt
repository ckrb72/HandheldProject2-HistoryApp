package com.example.project2historyapp

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.net.URLEncoder

object QueryManager {
    val client: OkHttpClient

    init {
        val builder = OkHttpClient.Builder()
        val loggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(loggingInterceptor)
        client = builder.build()
    }

    suspend fun retrieveHistoricalEvents(location: LatLng, radius: Int): List<HistoricalEvent> {

        val query: String = """
            SELECT ?item ?itemLabel ?location ?distance WHERE {
              SERVICE wikibase:around {
                ?item wdt:P625 ?location .
                bd:serviceParam wikibase:center "Point(${location.longitude} ${location.latitude})"^^geo:wktLiteral .
                bd:serviceParam wikibase:radius "$radius" .
                bd:serviceParam wikibase:distance ?distance .
              }

              # Broader categories
              VALUES ?class {
                wd:Q839954      # historic site
                wd:Q271669      # archaeological site
                wd:Q35509       # conflict / battle
                wd:Q57821       # monument
                wd:Q4989906     # heritage site
                wd:Q570116      # historic district
              }

              ?item wdt:P31/wdt:P279* ?class .

              SERVICE wikibase:label { bd:serviceParam wikibase:language "[AUTO_LANGUAGE],en". }
            }
            ORDER BY ?distance

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
                    val event = HistoricalEvent(
                        name = currentEvent.getJSONObject("itemLabel").getString("value")
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

}