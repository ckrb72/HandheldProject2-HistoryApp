package com.example.project2historyapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
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
    var mostVisitedCountry by remember { mutableStateOf<LocationVisitCount>(LocationVisitCount()) }
    var mostSavedCountry by remember { mutableStateOf("") }
    // stats/continents/list_of_continents_with_count
    // stats/countries/list_of_countries_with_count

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
                        if (location.count > locationCount) {
                            locationCount = location.count
                            mostVisitedCountry = location
                        }
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ERROR", error.message)
            }
        })

        val locationRef = FirebaseDatabase.getInstance().getReference("users/$user/locations")
        locationRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    locationCount = 0
                } else {
                    locationCount = snapshot.childrenCount
                }

                // Count up each instance of each country
                val countryMap = mutableMapOf<String, Long>()
                snapshot.children.mapNotNull { it.getValue(LocationData::class.java) }
                    .forEach { location ->
                    countryMap[location.country] = countryMap.getOrDefault(location.country, 0) + 1
                }
                mostSavedCountry = countryMap.maxBy { it.value }.key

            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    Column(
        modifier = modifier.fillMaxSize()
            .paint(
                painterResource(R.drawable.stats_background),
                contentScale = ContentScale.FillBounds
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.8f)
                .fillMaxHeight(0.9f)
                .alpha(0.75f),
            shape = RectangleShape
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("User: ")
                Text("Most Visited Continent")
                Text("Most Visited Country: ${mostVisitedCountry.name}")
                Text("Saved Locations: $locationCount")
                Text("Most Visited Time Period")
                Text("Favorite Time Period (most saved time period)")
                Text("Favorite Country (most saved country): $mostSavedCountry")
            }
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