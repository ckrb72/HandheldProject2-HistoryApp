package com.example.project2historyapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.example.project2historyapp.ui.theme.Project2HistoryAppTheme
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        val app = FirebaseApp.getInstance()
        Log.d("FBINit", "Firebase initialized: ${app.name}")
        enableEdgeToEdge()
        setContent {
            Project2HistoryAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Login(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Login(modifier: Modifier = Modifier) {

    var loginRequested by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("history_prefs", Context.MODE_PRIVATE) }
    var saveLoginInfo by remember { mutableStateOf(prefs.getBoolean("saveLogin", false)) }
    var email by remember { mutableStateOf( if (saveLoginInfo) prefs.getString("savedEmail", "").toString() else "") }
    var password by remember { mutableStateOf(if (saveLoginInfo) prefs.getString("savedPassword", "").toString() else "") }
    Box(
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = modifier.fillMaxSize()
                .paint(
                    painter = painterResource(R.drawable.vertical_globe),
                    contentScale = ContentScale.FillBounds
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = email,
                    placeholder = { Text("Email") },
                    onValueChange = {newVal -> email = newVal},
                    shape = RectangleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedPlaceholderColor = Color.White,
                        unfocusedPlaceholderColor = Color.White,
                        unfocusedContainerColor = Color(0.204f, 0.408f, 0.357f, 0.827f),
                        focusedContainerColor = Color(0.596f, 0.808f, 0.757f, 0.827f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                    )
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    placeholder = { Text("Password") },
                    onValueChange = {newVal -> password = newVal},
                    shape = RectangleShape,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedPlaceholderColor = Color.White,
                        unfocusedPlaceholderColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        unfocusedContainerColor = Color(0.204f, 0.408f, 0.357f, 0.827f),
                        focusedContainerColor =  Color(0.596f, 0.808f, 0.757f, 0.827f)
                    )
                )

                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(Color(0.204f, 0.408f, 0.357f, 0.8f))
                ) {
                    Text(
                        "Save Login Info",
                        color = Color.White,
                        modifier = Modifier.padding(10.dp)
                    )
                    Switch(
                        onCheckedChange = { checked ->
                            saveLoginInfo = checked
                        },
                        checked = saveLoginInfo,
                        modifier = Modifier.padding(5.dp, 0.dp, 5.dp, 0.dp)
                    )
                }
            }

            Button(
                onClick = {
                    loginRequested = true
                    prefs.edit { putBoolean("saveLogin", saveLoginInfo) }
                    if (saveLoginInfo) {
                        prefs.edit { putString("savedEmail", email) }
                        prefs.edit { putString("savedPassword", password) }
                    }
                },
                colors = ButtonColors(Color(0.616f, 0.494f, 0.337f, 1.0f), Color.White, Color(0.204f, 0.408f, 0.357f, 0.827f), Color.LightGray),
                shape = RectangleShape,
                modifier = Modifier.padding(20.dp),
                enabled = email.isNotBlank() && password.isNotBlank(),
            ) {
                Text("Login")
            }

            Button(
                onClick = {
                    val intent = Intent(context, RegisterActivity::class.java);
                    context.startActivity(intent)
                },
                colors = ButtonColors(Color(0.549f, 0.424f, 0.282f, 1.0f), Color.White, Color(0.204f, 0.408f, 0.357f, 0.827f), Color.LightGray),
                shape = RectangleShape,
                modifier = Modifier.padding(20.dp)
            ) {
                Text("Sign Up")
            }
        }
        error?.let {
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(0.75f),
                colors = CardColors(Color(0.824f, 0.169f, 0.169f, 1.0f), Color(0.204f, 0.408f, 0.357f, 0.827f), Color(0.204f, 0.408f, 0.357f, 0.827f), Color(0.204f, 0.408f, 0.357f, 0.827f)),
                shape = RectangleShape
            ) {
                Text(it, color = Color.White, textAlign = TextAlign.Center)
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                color = Color(1.0f, 0.718f, 0.0f, 1.0f),
                strokeWidth = 8.dp
            )
        }
    }

    LaunchedEffect(Unit) {
        val events = withContext(Dispatchers.IO) {
            QueryManager.retrieveHistoricalEvents(LatLng(38.8977, -77.0365), 10)
        }

        Log.d("TESTING", "${events.size}")
    }


    LaunchedEffect(loginRequested) {
        if (loginRequested) {
            isLoading = true
            try {
                error = null
                AuthRepository.login(email, password)
                val intent = Intent(context, MainMenuActivity::class.java)
                intent.putExtra("EMAIL", email)
                context.startActivity(intent)
            } catch (e: Exception) {
                error = e.message
                Log.d("AUTH", e.message.toString())
            } finally {
                loginRequested = false
                isLoading=false
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    Project2HistoryAppTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Login(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}