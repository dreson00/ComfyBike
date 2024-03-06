package com.bk.bk1.compose

import android.location.Location
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bk.bk1.utilities.SensorService
import com.bk.bk1.viewModels.MainMapScreenViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.leinardi.android.speeddial.compose.FabWithLabel
import com.leinardi.android.speeddial.compose.SpeedDial
import com.leinardi.android.speeddial.compose.SpeedDialState

@Composable
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
fun MainMapScreen(
    navController: NavController,
    viewModel: MainMapScreenViewModel,
    sensorServiceStopper: () -> Unit
) {

    var speedDialState by rememberSaveable { mutableStateOf(SpeedDialState.Collapsed) }
    var overlayVisible by rememberSaveable { mutableStateOf(speedDialState.isExpanded()) }

    val cameraPositionState = rememberCameraPositionState()

    val location: Location? by viewModel.location.observeAsState(null)
    val connectionStatus: Int? by SensorService.deviceInfo.connectionState.observeAsState(null)


    LaunchedEffect(location) {
        if (viewModel.cameraFollow && location != null) {
            cameraPositionState.animate(
                CameraUpdateFactory
                    .newLatLng(LatLng(location!!.latitude, location!!.longitude))
            )
            if (cameraPositionState.position.zoom < 15F) {
                cameraPositionState.animate(CameraUpdateFactory.zoomTo(15F))
            }
        }
    }

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
                if (connectionStatus == 0) {
                    item {
                        FabWithLabel(
                            onClick = { navController.navigate("sensorConnectScreen") },
                            labelContent = { Text(text = "Připojit senzor") },
                        ) {
                            Icon(Icons.Default.Add, null)
                        }
                    }
                }
                else {
                    item {
                        FabWithLabel(
                            onClick = { sensorServiceStopper() },
                            labelContent = { Text(text = "Odpojit senzor") },
                        ) {
                            Icon(Icons.Default.Delete, null)
                        }
                    }
                }
            }
        },
        content = { padding ->
            Box(modifier = Modifier.padding(padding)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
                    properties = MapProperties(isMyLocationEnabled = true),
                    cameraPositionState = cameraPositionState
                ) {
                    LaunchedEffect(cameraPositionState.isMoving) {
                        if (cameraPositionState.isMoving && cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
                            viewModel.disableCameraFollow()
                            speedDialState = SpeedDialState.Collapsed
                        }
                    }
                }
                Text(
                    text = connectionStatus.toString(),
                    color = Color.Black)
                FloatingActionButton(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    onClick = {
                        viewModel.toggleCameraFollow()
                    }
                ) {
                    if (viewModel.cameraFollow) {
                        Icon(Icons.Filled.Favorite, contentDescription = "Moje poloha")
                    }
                    else {
                        Icon(Icons.Filled.FavoriteBorder, contentDescription = "Moje poloha")
                    }
                }
            }
        }
    )
}

//item {
//    FabWithLabel(
//        onClick = { },
//        labelContent = { Text(text = "Item 1") },
//    ) {
//        Icon(Icons.Default.Share, null)
//    }
//}