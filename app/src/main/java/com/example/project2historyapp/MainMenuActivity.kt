package com.example.project2historyapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.project2historyapp.ui.theme.Project2HistoryAppTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainMenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Project2HistoryAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val user = intent.getStringExtra("EMAIL")
                    user?.let {
                        MyMap(user, Modifier.padding(innerPadding))
                    }
                    MyMap("UNDEFINED_USER", Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MyMap(user: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val staffordVA = LatLng(38.4221, -77.4083)
    var markerPosition by remember { mutableStateOf<LatLng?>(staffordVA) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(staffordVA, 10f)
    }
    var isLoading by remember { mutableStateOf(false) }
    var addressInfo by remember { mutableStateOf("Long Click on Map") }
    // Map Style JSON and code from Google Maps Documentation
    val mapStyle = remember { MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            useCurrentLocation(context) { latLng ->
                markerPosition = latLng
            }
        }
    }
    var showMenu by remember { mutableStateOf(false) }

    // Check if the location permission is granted, if not, request it
    LaunchedEffect(Unit) {
        if (!checkLocationPermission(context)) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Box(
        contentAlignment = Alignment.Center
    ) {
        GoogleMap(
            modifier = modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(mapStyleOptions = mapStyle),  // From Google Maps Documentation
            onMapLongClick = { latLng ->
                markerPosition = latLng
                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, cameraPositionState.position.zoom)
//                prefs.edit { putFloat("MapLatitude", latLng.latitude.toFloat()) }
//                prefs.edit { putFloat("MapLongitude", latLng.longitude.toFloat()) }
                addressInfo = "Resolving address..."
            }
        ) {
            markerPosition?.let { position ->
                Marker(
                    state = MarkerState(position = position),
                    title = "Results for: $addressInfo",
                    snippet = "(" + position.latitude + ", " + position.longitude + ")"
                )
            }

        }

        if (isLoading) {
            CircularProgressIndicator(
                color = Color(1.0f, 0.718f, 0.0f, 1.0f),
                strokeWidth = 8.dp
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            if (showMenu) {
                Card(
                    modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 5.dp),
                    colors = CardColors(Color.White, Color.White, Color.White, Color.White),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            shape = RectangleShape,
                            onClick = {
                                val intent = Intent(context, StatisticsActivity::class.java)
                                intent.putExtra("EMAIL", user)
                                context.startActivity(intent)
                            }
                        ) {
                            Text("Statistics")
                        }

                        Button(
                            shape = RectangleShape,
                            onClick = {
                                val intent = Intent(context, SavedLocationsActivity::class.java)
                                intent.putExtra("EMAIL", user)
                                context.startActivity(intent)
                            }
                        ) {
                            Text("Saved Locations")
                        }

                        Button(
                            shape = RectangleShape,
                            onClick = {
//                                val intent = Intent(context, DateCalendarActivity::class.java)
//                                context.startActivity(intent)
                            }
                        ) {
                            Text("Set Date")
                        }
                    }
                }
            }

            FloatingActionButton(
                modifier = Modifier.padding(45.dp, 0.dp, 0.dp, 50.dp),
                onClick = {
                    showMenu = !showMenu
                }
            ) {
                Icon(
                    painter = painterResource(if (showMenu) R.drawable.outline_arrow_drop_up_24 else R.drawable.outline_arrow_drop_down_24),
                    contentDescription = "Menu"
                )
            }
        }
    }

    LaunchedEffect(markerPosition) {
        isLoading = true
        markerPosition?.let { latLng ->
            addressInfo = withContext(Dispatchers.IO) {
                getAddressGeocodeCurrent(context, latLng)
            }
            isLoading = false
            Log.d("Address", addressInfo)
        }
    }
}

fun checkLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

fun useCurrentLocation(context: Context, onLocationResult: (LatLng) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val cancellationTokenSource = CancellationTokenSource()

    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token).addOnSuccessListener{ location ->
        if (location != null) {
            onLocationResult(LatLng(location.latitude, location.longitude))
        } else {
            onLocationResult(LatLng(38.4221, -77.4083))
        }
    }
}

suspend fun getAddressGeocodeCurrent(context: android.content.Context, latLng: LatLng): String =
    suspendCoroutine { continuation ->
        val geocoder = Geocoder(context, Locale.getDefault())
        geocoder.getFromLocation(
            latLng.latitude,
            latLng.longitude,
            1,
            object : Geocoder.GeocodeListener {
                override fun onGeocode(addressList: MutableList<Address>) {
                    val result = if (addressList.isNotEmpty()) {
                        val address = addressList[0]
                        val city = address.locality ?: "Unknown City"
                        val state = address.adminArea ?: "Unknown State"
                        val country = address.countryName ?: "Unknown Country"
                        "$city, $state, $country"
                    } else {
                        "No address found."
                    }
                    continuation.resume(result)
                }

                override fun onError(errorMessage: String?) {
                    continuation.resume("Geocoding failed: ${errorMessage ?: "Unknown error"}")
                }
            }
        )
    }

@Preview(showBackground = true)
@Composable
fun MapPreview() {
    Project2HistoryAppTheme {
        MyMap("UNDEFINED_USER")
    }
}