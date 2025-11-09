package com.example.project2historyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.example.project2historyapp.ui.theme.Project2HistoryAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize()
            .background(Color.Gray),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ancient_earth_globe_stockcake),
            contentDescription = "Image of Globe"
        )

        OutlinedTextField(
            value = username,
            onValueChange = {newVal -> username = newVal},
            modifier = Modifier.background(Color.White),
            shape = RectangleShape
        )

        OutlinedTextField(
            value = password,
            onValueChange = {newVal -> password = newVal},
            modifier = Modifier.background(Color.White),
            shape = RectangleShape,
            visualTransformation = PasswordVisualTransformation()
        )

        Button(
            onClick = {},
            colors = ButtonColors(Color.Black, Color.White, Color.DarkGray, Color.LightGray),
            shape = RectangleShape
        ) {
            Text("Login")
        }

        Button(
            onClick = {},
            colors = ButtonColors(Color.Black, Color.White, Color.DarkGray, Color.LightGray),
            shape = RectangleShape
        ) {
            Text("Sign Up")
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