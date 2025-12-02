package com.example.project2historyapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project2historyapp.ui.theme.Project2HistoryAppTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StatisticsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val user = intent.getStringExtra("EMAIL").toString()
        setContent {
            Project2HistoryAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Stats(
                        user,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Stats(user: String, modifier: Modifier = Modifier) {
    var locationCount by remember { mutableLongStateOf(0) }
    var mostVisitedCountry by remember { mutableStateOf(LocationVisitCount()) }
    var mostVisitedCountryCount by remember { mutableLongStateOf(0) }
    var mostSavedCountry by remember { mutableStateOf("None") }
    var mostSavedCountryCount by remember { mutableLongStateOf(0) }
    var searchInfo by remember { mutableStateOf(SearchData(0, 0.0)) }
    var eventCount by remember { mutableLongStateOf(0) }
    val context = LocalContext.current

    remember {
        val statsRef = FirebaseDatabase.getInstance().getReference("users/$user/countries")
            statsRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Log.d("ERROR", "No data")
                    return
                }

                snapshot.children.mapNotNull { it.getValue(LocationVisitCount::class.java) }
                    .forEach { location ->
                        if (location.count > mostVisitedCountryCount) {
                            mostVisitedCountryCount = location.count
                            mostVisitedCountry = location
                        }
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ERROR", error.message)
            }
        })

        val eventsRef = FirebaseDatabase.getInstance().getReference("users/$user/events")
        eventsRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    eventCount = 0
                    return
                }
                eventCount = snapshot.childrenCount
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })

        val searchesRef = FirebaseDatabase.getInstance().getReference("users/$user/searches")
        searchesRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    searchInfo = SearchData(0, 0.0)
                    return
                }

                snapshot.getValue(SearchData::class.java)?.let { data ->
                    searchInfo = data
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })


        val locationRef = FirebaseDatabase.getInstance().getReference("users/$user/locations")
        locationRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    locationCount = 0
                    mostSavedCountry = "None"
                } else {
                    locationCount = snapshot.childrenCount

                    // Count up each instance of each country
                    val countryMap = mutableMapOf<String, Long>()
                    snapshot.children.mapNotNull { it.getValue(LocationData::class.java) }
                        .forEach { location ->
                            countryMap[location.country] = countryMap.getOrDefault(location.country, 0) + 1
                        }
                    mostSavedCountry = countryMap.maxBy { it.value }.key
                    mostSavedCountryCount = countryMap.maxBy { it.value }.value
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    Box(
        modifier = modifier.fillMaxSize()
            .paint(
                painterResource(R.drawable.stats_background),
                contentScale = ContentScale.FillBounds
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.85f)
                .fillMaxHeight(0.9f)
                .background(Color(0.906f, 0.843f, 0.639f, 0.659f)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,

        ) {
            Text(
                "${context.getString(R.string.statistics_button)}:",
                fontSize = 25.sp,
                color = Color(0.267f, 0.165f, 0.02f, 1.0f),
                textAlign = TextAlign.Center
            )
            Column(
                modifier = Modifier.fillMaxWidth()
                    .fillMaxHeight(0.75f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    "${context.getString(R.string.most_visited_country)}: ${mostVisitedCountry.name} ($mostVisitedCountryCount)",
                    color = Color(0.267f, 0.165f, 0.02f, 1.0f),
                    textAlign = TextAlign.Center
                )
                Text(
                    "${context.getString(R.string.saved_locations_count)}: $locationCount",
                    color = Color(0.267f, 0.165f, 0.02f, 1.0f),
                    textAlign = TextAlign.Center
                )
                Text(
                    "${context.getString(R.string.saved_events_count)}: $eventCount",
                    color = Color(0.267f, 0.165f, 0.02f, 1.0f),
                    textAlign = TextAlign.Center
                )
                Text(
                    "${context.getString(R.string.average_search_radius)}: ${searchInfo.avgRadius}",
                    color = Color(0.267f, 0.165f, 0.02f, 1.0f),
                    textAlign = TextAlign.Center
                )
                Text(
                    "${context.getString(R.string.most_saved_country)}: $mostSavedCountry ($mostSavedCountryCount)",
                    color = Color(0.267f, 0.165f, 0.02f, 1.0f),
                    textAlign = TextAlign.Center
                )
                Text(
                    "${context.getString(R.string.searches_text)}: ${searchInfo.count}",
                    color = Color(0.267f, 0.165f, 0.02f, 1.0f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Button(
            shape = RectangleShape,
            colors = ButtonColors(Color(0.616f, 0.494f, 0.337f, 1.0f), Color.White, Color(0.204f, 0.408f, 0.357f, 0.827f), Color.LightGray),
            modifier = modifier.align(Alignment.BottomCenter)
                .padding(0.dp, 0.dp, 0.dp, 35.dp),
            onClick = {
                val intent = Intent(context, MainMenuActivity::class.java)
                intent.putExtra("EMAIL", user)
                context.startActivity(intent)
            }
        ) {
            Text(
                context.getString(R.string.back_text),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Project2HistoryAppTheme {
        Stats("Ciaran")
    }
}