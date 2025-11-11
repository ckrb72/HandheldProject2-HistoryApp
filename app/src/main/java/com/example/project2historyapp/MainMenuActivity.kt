package com.example.project2historyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.project2historyapp.ui.theme.Project2HistoryAppTheme
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

class MainMenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Project2HistoryAppTheme {
                MyMap()
            }
        }
    }
}

@Composable
fun MyMap() {
    val staffordVA = LatLng(38.4221, -77.4083)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(staffordVA, 10f)
    }

    val context = LocalContext.current

    // Map Style JSON and code from Google Maps Documentation
    val mapStyle = remember { MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style) }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapStyleOptions = mapStyle)  // From Google Maps Documentation
    ) {
        Marker(
            state = MarkerState(position =staffordVA),
            title = "Stafford",
            //title=stringResource(R.string.default_city_london),
            snippet = "Marker in stafford"
        )

    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Project2HistoryAppTheme {
        MyMap()
    }
}