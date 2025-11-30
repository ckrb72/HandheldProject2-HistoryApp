package com.example.project2historyapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.project2historyapp.ui.theme.Project2HistoryAppTheme

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Project2HistoryAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Register(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Register(modifier: Modifier = Modifier) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmedPassword by remember { mutableStateOf("") }
    var registerRequested by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

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
                modifier = Modifier.padding(10.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    placeholder = {
                        val emailString = stringResource(R.string.email_placeholder)
                        Text(emailString) },
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
                    placeholder = {
                        val passwordPlaceholder = stringResource(R.string.password_placeholder)
                        Text(passwordPlaceholder) },
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

                OutlinedTextField(
                    value = confirmedPassword,
                    placeholder = {
                        val confirmPasswordPlaceholder = stringResource(R.string.confirm_password_placeholder)
                        Text(confirmPasswordPlaceholder) },
                    onValueChange = {newVal -> confirmedPassword = newVal},
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
            }

            val passwordMatchError = stringResource(R.string.password_match_error)
            Button(
                onClick = {
                    // Check to make sure the two passwords are the same
                    error = null
                    if (password != confirmedPassword) {
                        error = passwordMatchError
                    } else {
                        registerRequested = true
                    }
                },
                colors = ButtonColors(Color(0.549f, 0.424f, 0.282f, 1.0f), Color.White, Color(0.204f, 0.408f, 0.357f, 0.827f), Color.LightGray),
                shape = RectangleShape,
                modifier = Modifier.padding(20.dp),
                enabled = email.isNotBlank() && password.isNotBlank() && confirmedPassword.isNotBlank()
            ) {
                val registerString = stringResource(R.string.register_text)
                Text(registerString)
            }

            Button(
                onClick = {
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                },
                colors = ButtonColors(Color(0.549f, 0.424f, 0.282f, 1.0f), Color.White, Color(0.204f, 0.408f, 0.357f, 0.827f), Color.LightGray),
                shape = RectangleShape,
                modifier = Modifier.padding(20.dp)
            ) {
                val backString = stringResource(R.string.back_text)
                Text(backString)
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                color = Color(1.0f, 0.718f, 0.0f, 1.0f),
                strokeWidth = 8.dp
            )
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
    }

    LaunchedEffect(registerRequested) {
        if (registerRequested) {
            isLoading = true
            try {
                error = null
                AuthRepository.register(email, password)
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            } catch (e: Exception) {
                error = e.message
                Log.d("AUTH", e.message.toString())
            } finally {
                registerRequested = false
                isLoading = false
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    Project2HistoryAppTheme {
        Register()
    }
}