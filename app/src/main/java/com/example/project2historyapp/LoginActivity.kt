package com.example.project2historyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize()
            .background(Color.Gray),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ancient_earth_globe_stockcake),
            contentDescription = "Image of Globe",
            modifier = Modifier.padding(20.dp)
        )

        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            OutlinedTextField(
                value = email,
                placeholder = { Text("Email") },
                onValueChange = {newVal -> email = newVal},
                shape = RectangleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedPlaceholderColor = Color.White,
                    unfocusedPlaceholderColor = Color.White,
                    unfocusedContainerColor = Color.Black,
                    focusedContainerColor = Color.DarkGray,
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
                    unfocusedContainerColor = Color.Black,
                    focusedContainerColor = Color.DarkGray
                )
            )
        }
        Button(
            onClick = {},
            colors = ButtonColors(Color.Black, Color.White, Color.DarkGray, Color.LightGray),
            shape = RectangleShape,
            modifier = Modifier.padding(20.dp)
        ) {
            Text("Login")
        }

        Button(
            onClick = {},
            colors = ButtonColors(Color.Black, Color.White, Color.DarkGray, Color.LightGray),
            shape = RectangleShape,
            modifier = Modifier.padding(20.dp)
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