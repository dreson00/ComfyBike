package com.bk.bk1

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bk.bk1.ui.theme.BK1Theme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.leinardi.android.speeddial.compose.FabWithLabel
import com.leinardi.android.speeddial.compose.SpeedDial
import com.leinardi.android.speeddial.compose.SpeedDialState

class MainActivity : ComponentActivity() {

    private val MY_PERMISSIONS_REQUEST_BLUETOOTH_AND_LOCATION = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        checkPermissions()
        setContent {
            BK1Theme {
                Map()
                Scaffold()
            }
        }
    }

    @Composable
    private fun Map() {
        val cameraPositionState = rememberCameraPositionState()
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = true),
            properties = MapProperties(isMyLocationEnabled = true)
        )
    }

    @Composable
    @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
    private fun Scaffold() {
        var speedDialState by rememberSaveable { mutableStateOf(SpeedDialState.Collapsed) }
        var overlayVisible by rememberSaveable { mutableStateOf(speedDialState.isExpanded()) }

        Scaffold(
            floatingActionButton = {
                SpeedDial(
                    fabClosedContent = { Icon(Icons.Filled.Menu, null) },
                    state = speedDialState,
                    onFabClick = { expanded ->
                        overlayVisible = !expanded
                        speedDialState = if (speedDialState == SpeedDialState.Collapsed) {
                            SpeedDialState.Expanded
                        } else {
                            SpeedDialState.Collapsed
                        }

                    }
                ) {
                    item {
                        FabWithLabel(
                            onClick = { },
                            labelContent = { Text(text = "Item 1") },
                        ) {
                            Icon(Icons.Default.Share, null)
                        }
                    }
                    item {
                        FabWithLabel(
                            onClick = { },
                            labelContent = { Text(text = "Item 1") },
                        ) {
                            Icon(Icons.Default.Share, null)
                        }
                    }
                }
            },
            content = { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    Map()
                }
            }
        )
    }

    @Preview
    @Composable
    private fun Preview(){
        Map()
        Scaffold()
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH)
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                MY_PERMISSIONS_REQUEST_BLUETOOTH_AND_LOCATION)
        }
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_BLUETOOTH_AND_LOCATION -> {
                if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    checkPermissions()
                }
                return
            }
        }
    }
}




//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    BK1Theme {
//        Greeting("Android")
//    }
//}