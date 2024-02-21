package com.bk.bk1

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
import com.bk.bk1.ui.theme.BK1Theme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.leinardi.android.speeddial.compose.FabWithLabel
import com.leinardi.android.speeddial.compose.SpeedDial
import com.leinardi.android.speeddial.compose.SpeedDialOverlay
import com.leinardi.android.speeddial.compose.SpeedDialState

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BK1Theme {
                map()
                scaffold()
            }
        }
    }

    @Composable
    private fun map() {
        val singapore = LatLng(1.35, 103.87)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(singapore, 10f)
        }
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        )
    }

    @Composable
    @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
    private fun scaffold() {
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
                    SpeedDialOverlay(
                        visible = overlayVisible,
                        onClick = {
                            overlayVisible = false
                            speedDialState = speedDialState.toggle()
                        },
                    )
                }
            }
        )
    }

    @Preview
    @Composable
    private fun preview(){
        map()
        scaffold()
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