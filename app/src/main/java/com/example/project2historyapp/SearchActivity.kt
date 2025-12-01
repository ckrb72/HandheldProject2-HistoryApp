package com.example.project2historyapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val locationName = intent.getStringExtra("LOCATION_NAME").toString()
        val startTime = intent.getLongExtra("START_TIME", 0)
        val endTime = intent.getLongExtra("END_TIME", 0)
        val latitude = intent.getDoubleExtra("LATITUDE", 0.0)
        val longitude = intent.getDoubleExtra("LONGITUDE", 0.0)
        val user = intent.getStringExtra("EMAIL").toString()
        setContent {
            Project2HistoryAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LocationSearch(
                        "Ciaran",
                        LatLng(latitude, longitude),
                        startTime,
                        endTime,
                        locationName,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun LocationSearch(user: String, latLng: LatLng, startTime: Long, endTime: Long, locationName: String, modifier: Modifier = Modifier) {
    var eventList by remember { mutableStateOf<List<HistoricalEvent>>(listOf()) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        isLoading = true

        val result = withContext(Dispatchers.IO) {
            QueryManager.retrieveHistoricalEvents(latLng, 100)
        }

        eventList = result
        isLoading = false
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
            modifier = Modifier.fillMaxWidth(0.8f)
                .fillMaxHeight(0.9f)
                .background(Color(1.0f, 1.0f, 1.0f, 0.75f)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "${context.getString(R.string.event_text)}: $locationName",
                fontSize = 20.sp,
                modifier = Modifier.padding(20.dp),
                textAlign = TextAlign.Center
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(10.dp, 0.dp, 10.dp, 30.dp)
            ) {
                items(eventList) { event ->
                    EventCard(event, onClick = { /*TODO*/ })
                }
            }
        }

        Row(
            modifier = modifier.align(Alignment.BottomCenter)
                .padding(0.dp, 0.dp, 0.dp, 50.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                shape = RectangleShape,
                colors = ButtonColors(Color(0.616f, 0.494f, 0.337f, 1.0f), Color.White, Color(0.204f, 0.408f, 0.357f, 0.827f), Color.LightGray),
                onClick = {
                    val intent = Intent(context, MainMenuActivity::class.java)
                    intent.putExtra("EMAIL", user)
                    context.startActivity(intent)
                }
            ) {
                Text(context.getString(R.string.back_text))
            }

            Button(
                shape = RectangleShape,
                colors = ButtonColors(Color(0.616f, 0.494f, 0.337f, 1.0f), Color.White, Color(0.204f, 0.408f, 0.357f, 0.827f), Color.LightGray),
                onClick = {
                    // Save location to database
                    val locationRef = Firebase.database.getReference("users/$user/locations")
                    val child = locationRef.child(locationName)
                    child.setValue(LocationData(latLng.latitude, latLng.longitude, startTime, endTime, locationName))
                }
            ) {
                Text(context.getString(R.string.save_location_text))
            }

        }


        if (isLoading) {
            CircularProgressIndicator(
                color = Color(1.0f, 0.718f, 0.0f, 1.0f),
                strokeWidth = 8.dp
            )
        }
    }
}

@Composable
fun EventCard(savedLocation: HistoricalEvent, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.fillMaxSize()
            .padding(10.dp),
        shape = RectangleShape,
        onClick = onClick
    ) {
        Column {
            Text(savedLocation.name)
            Text(savedLocation.date)
            Text("${savedLocation.location.latitude} ${savedLocation.location.longitude}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview4() {
    Project2HistoryAppTheme {
        LocationSearch("Ciaran", LatLng(0.0, 0.0), 0, 0, "Buffalo")
    }
}