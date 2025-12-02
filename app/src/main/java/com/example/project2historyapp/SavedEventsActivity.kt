package com.example.project2historyapp

import android.content.Intent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.core.net.toUri
import com.example.project2historyapp.ui.theme.Project2HistoryAppTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SavedEventsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val user = intent.getStringExtra("EMAIL").toString()
        setContent {
            Project2HistoryAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SavedEvents(
                        user,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun SavedEvents(user: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val eventList = remember { mutableStateListOf<HistoricalEvent>() }
    var isLoading by remember { mutableStateOf(false) }
    Log.d("USER", user)

    remember {
        val dbRef = FirebaseDatabase.getInstance().getReference("users/$user/events")

        dbRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    eventList.clear()
                    return
                }
                eventList.clear()
                snapshot.children.mapNotNull { it.getValue(HistoricalEvent::class.java) }
                    .forEach { eventList.add(it) }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ERROR", error.message)
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
        ) {
            Text(
                context.getString(R.string.saved_locations_button),
                fontSize = 25.sp,
                modifier = Modifier.padding(20.dp),
                color = Color(0.267f, 0.165f, 0.02f, 1.0f)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
                    .padding(10.dp, 0.dp, 10.dp, 30.dp)
            ) {
                items(eventList) { event ->
                    EventRemovableCard(
                        event,
                        onClick = {
                            if (event.article != null) {
                                val intent = Intent(Intent.ACTION_VIEW, event.article.toUri())
                                context.startActivity(intent)
                            } else {
                                Toast.makeText(context, context.getString(R.string.no_article_text), Toast.LENGTH_SHORT).show()
                            }
                        },
                        onRemove = {
                            val dbRef = FirebaseDatabase.getInstance().getReference("users/$user/events")
                            dbRef.child(event.name).removeValue()
                        }
                    )
                }
            }
        }

        Row(
            modifier = modifier.align(Alignment.BottomCenter)
                .padding(0.dp, 0.dp, 0.dp, 35.dp)
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

        }

        if (eventList.isEmpty()) {
            Text(
                context.getString(R.string.no_saved_locations_text),
                modifier = Modifier.align(Alignment.Center),
                fontSize = 20.sp,
                color = Color(0.267f, 0.165f, 0.02f, 1.0f)
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
fun EventRemovableCard(savedLocation: HistoricalEvent, modifier: Modifier = Modifier, onClick: () -> Unit, onRemove: () -> Unit) {
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
                    onClick = onRemove
                ) {
                    Text(
                        context.getString(R.string.remove_text),
                        textAlign = TextAlign.Center,
                        fontSize = 8.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview5() {
    Project2HistoryAppTheme {
        SavedEvents("Ciaran")
    }
}