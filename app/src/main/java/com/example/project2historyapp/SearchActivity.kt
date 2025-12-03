package com.example.project2historyapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material3.CardColors
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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
        val radius = intent.getIntExtra("RADIUS", 0)

        val locationInfo = locationName.split(", ")
        val countryRef = Firebase.database.getReference("users/$user/countries")
        val child = countryRef.child(locationInfo[2])
        child.runTransaction(object: Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val data = currentData.getValue(LocationVisitCount::class.java) ?: LocationVisitCount(locationInfo[2], 0)
                data.count++
                currentData.value = data
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?
            ) {

            }
        })

        Firebase.database
            .getReference("users/$user/searches")
            .runTransaction(object: Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val data = currentData.getValue(SearchData::class.java) ?: SearchData(0, 0.0)
                val radiusSum = data.avgRadius * data.count
                data.count++
                data.avgRadius = (radiusSum + radius) / data.count
                currentData.value = data
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?
            ) {

            }
        })


        setContent {
            Project2HistoryAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LocationSearch(
                        user,
                        LatLng(latitude, longitude),
                        startTime,
                        endTime,
                        locationName,
                        radius,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun LocationSearch(user: String, latLng: LatLng, startTime: Long, endTime: Long, locationName: String, radius: Int, modifier: Modifier = Modifier) {
    var eventList by remember { mutableStateOf<List<HistoricalEvent>>(listOf()) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val startString by remember { mutableStateOf(convertMillisToDate(startTime)) }
    val endString by remember { mutableStateOf(convertMillisToDate(endTime)) }
    LaunchedEffect(Unit) {
        isLoading = true

        val result = withContext(Dispatchers.IO) {
            QueryManager.retrieveHistoricalEvents(latLng, startString, endString, radius)
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
            modifier = Modifier.fillMaxWidth(0.85f)
                .fillMaxHeight(0.9f)
                .background(Color(0.906f, 0.843f, 0.639f, 0.659f)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "${context.getString(R.string.event_text)}: $locationName",
                fontSize = 20.sp,
                modifier = Modifier.padding(20.dp),
                textAlign = TextAlign.Center,
                color = Color(0.267f, 0.165f, 0.02f, 1.0f),
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(10.dp, 0.dp, 10.dp, 30.dp)
            ) {
                items(eventList) { event ->
                    EventCard(
                        event,
                        onClick = {
                            if (event.article != null) {
                                val intent = Intent(Intent.ACTION_VIEW, event.article.toUri())
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, context.getString(R.string.no_article_text), Toast.LENGTH_SHORT).show()
                            }
                        },
                        onSave = {
                            val dbRef = Firebase.database.getReference("users/$user/events")
                            val child = dbRef.push()
                            event.dbKey = child.key.toString()
                            child.setValue(event)
                        }
                    )
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
                    val locationRef = Firebase.database.getReference("users/$user/locations")
                    val child = locationRef.push()
                    child.setValue(LocationData(latLng.latitude, latLng.longitude, startTime, endTime, locationName, locationName.split(", ")[2], child.key.toString()))
                }
            ) {
                Text(context.getString(R.string.save_location_text))
            }

        }

        if (eventList.isEmpty() && !isLoading) {
            Text(
                context.getString(R.string.no_events_text),
                fontSize = 20.sp,
                modifier = Modifier.padding(20.dp),
                textAlign = TextAlign.Center
            )
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
fun EventCard(savedLocation: HistoricalEvent, modifier: Modifier = Modifier, onClick: () -> Unit, onSave: () -> Unit) {
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxSize()
            .padding(10.dp),
        shape = RectangleShape,
        colors = CardColors(Color(0.686f, 0.62f, 0.42f, 1.0f), Color.White, Color.White, Color.White),
        onClick = onClick
    ) {

        Row(
            modifier = Modifier.fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Column(
                modifier = Modifier.fillMaxWidth(0.70f),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(savedLocation.name, textAlign = TextAlign.Center)
                Text(savedLocation.date, fontSize = 12.sp, textAlign = TextAlign.Center)
                Text("${"%.2f".format(savedLocation.latitude)} ${"%.2f".format(savedLocation.longitude)}", fontSize = 12.sp, textAlign = TextAlign.Center)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    modifier = Modifier.fillMaxHeight(0.30f)
                        .fillMaxWidth(),
                    shape = RectangleShape,
                    colors = ButtonColors(Color(0.616f, 0.494f, 0.337f, 1.0f), Color.White, Color(0.204f, 0.408f, 0.357f, 0.827f), Color.LightGray),
                    onClick = onSave
                ) {
                    Text(
                        context.getString(R.string.save_text),
                        textAlign = TextAlign.Center,
                        fontSize = 8.sp)
                }
            }
        }
    }
}

fun convertMillisToDate(millis: Long): String {
    val instant = Instant.ofEpochMilli(millis)
    val date = instant.atZone(ZoneOffset.UTC).toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    return date.format(formatter)
}

@Preview(showBackground = true)
@Composable
fun EventCardPreview() {
    Project2HistoryAppTheme {
        EventCard(HistoricalEvent("Test", "Test", 0.0, 0.0, "Test"), onClick = {}) {

        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview4() {
    Project2HistoryAppTheme {
        LocationSearch("Ciaran", LatLng(0.0, 0.0), 0, 0, "Buffalo", 100)
    }
}