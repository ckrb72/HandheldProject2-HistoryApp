package com.example.project2historyapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project2historyapp.ui.theme.Project2HistoryAppTheme
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher

class SavedLocationsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val user = intent.getStringExtra("EMAIL").toString()
        setContent {
            Project2HistoryAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SavedLocations(
                        "Ciaran",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun SavedLocations(user: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val locationList = remember { mutableStateListOf<LocationData>() }
    var isLoading by remember { mutableStateOf(false) }

    remember {
        val dbRef = FirebaseDatabase.getInstance().getReference("users/$user/locations")

        dbRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(context, "No Database Data Found", Toast.LENGTH_SHORT).show()
                    return
                }
                locationList.clear()
                snapshot.children.mapNotNull { it.getValue(LocationData::class.java) }
                    .forEach { locationList.add(it)}
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "No Data Found", Toast.LENGTH_SHORT).show()
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
            modifier = Modifier.fillMaxWidth(0.8f)
                .fillMaxHeight(0.9f)
                .background(Color(1.0f, 1.0f, 1.0f, 0.75f)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Saved Locations",
                fontSize = 25.sp,
                modifier = Modifier.padding(20.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(10.dp, 0.dp, 10.dp, 30.dp)
            ) {
                items(locationList) { location ->
                    LocationCard(location, onClick = {
                        val intent = Intent(context, SearchActivity::class.java)
                        intent.putExtra("EMAIL", user)
                        intent.putExtra("LATITUDE", location.latitude)
                        intent.putExtra("LONGITUDE", location.longitude)
                        intent.putExtra("START_TIME", location.start)
                        intent.putExtra("END_TIME", location.end)
                        intent.putExtra("LOCATION_NAME", location.name)
                        context.startActivity(intent)
                    })
                }
            }
        }

        if (locationList.isEmpty()) {
            Text(
                context.getString(R.string.no_saved_locations_text),
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp
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
fun LocationCard(savedLocation: LocationData, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.fillMaxSize()
            .padding(10.dp),
        shape = RectangleShape,
        onClick = onClick
    ) {
        Column {
            Text(savedLocation.name)
            Text("${savedLocation.latitude} ${savedLocation.longitude}")
            Text("${savedLocation.start} ${savedLocation.end}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview3() {
    Project2HistoryAppTheme {
        SavedLocations("Ciaran")
    }
}