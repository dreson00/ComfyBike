package com.bk.bk1.compose

import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import androidx.annotation.DrawableRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.navigation.NavController
import com.bk.bk1.R
import com.bk.bk1.utilities.SensorService
import com.bk.bk1.viewModels.MainMapScreenViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.leinardi.android.speeddial.compose.FabWithLabel
import com.leinardi.android.speeddial.compose.SpeedDial
import com.leinardi.android.speeddial.compose.SpeedDialState

@Composable
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
fun MainMapScreen(
    navController: NavController,
    viewModel: MainMapScreenViewModel,
    startSensorServiceWithAction: (String) -> Unit
) {

    var speedDialState by rememberSaveable { mutableStateOf(SpeedDialState.Collapsed) }
    var overlayVisible by rememberSaveable { mutableStateOf(speedDialState.isExpanded()) }

    val cameraPositionState = rememberCameraPositionState()

    val location: Location? by viewModel.location.observeAsState(null)
    val connectionStatus: Int by viewModel.connectionStatus.observeAsState(initial = 0)
    val trackingStatus: Int by viewModel.trackingStatus.observeAsState(initial = 0)
    val isCountdownOn: Boolean by viewModel.isCountdownOn.observeAsState(initial = false)
    val countDownProgress: Long by viewModel.countdownProgress.observeAsState(initial = 0L)


    LaunchedEffect(location) {
        if (viewModel.cameraFollow && location != null) {
            cameraPositionState.animate(
                CameraUpdateFactory
                    .newLatLng(LatLng(location!!.latitude, location!!.longitude))
            )
            if (cameraPositionState.position.zoom < 15F) {
                cameraPositionState.animate(CameraUpdateFactory.zoomTo(17F))
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
                            onClick = {
                                startSensorServiceWithAction(SensorService.Actions.STOP_ALL.toString())
                            },
                            labelContent = { Text(text = "Odpojit senzor") },
                        ) {
                            Icon(Icons.Default.Delete, null)
                        }
                    }
                }
                if (connectionStatus == 2 && trackingStatus == 0) {
                    item {
                        FabWithLabel(
                            onClick = {
                                startSensorServiceWithAction(SensorService.Actions.START_TRACKING.toString())
                            },
                            labelContent = { Text(text = "Zaznamenat novou trasu") },
                        ) {
                            Icon(Icons.Default.PlayArrow, null)
                        }
                    }
                }
                else if (trackingStatus == 1) {
                    item {
                        FabWithLabel(
                            onClick = {
                                startSensorServiceWithAction(SensorService.Actions.STOP_TRACKING.toString())
                            },
                            labelContent = { Text(text = "Ukončit zaznamenávání") },
                        ) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                }
                item {
                    FabWithLabel(
                        onClick = { viewModel.deleteAllTracks() },
                        labelContent = { Text(text = "Smazat všechny trasy") },
                    ) {
                        Icon(Icons.Default.Delete, null)
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
                    val comfortIndexRecords = viewModel.comfortIndexRecords.collectAsState(initial = emptyList())
                    if (comfortIndexRecords.value.isNotEmpty()) {
                        comfortIndexRecords.value.forEach { item ->
                            val color = Color.hsl(mapFloatToHue(item.comfortIndex), 1f, 0.5f)
                            MapMarker(
                                context = LocalContext.current,
                                position = LatLng(item.latitude, item.longitude),
                                title = "CI: ${item.comfortIndex}",
                                color = color,
                                iconResourceId = R.drawable.baseline_circle_12
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .height(35.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Stav senzoru:",
                            color = Color.White,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )

                        val sensorStatusText = when(connectionStatus) {
                            0 -> "Nepřipojen"
                            1 -> "Připojování..."
                            2 -> "Připojen"
                            else -> "Chyba"
                        }
                        Text(
                            text = sensorStatusText,
                            color = Color.White,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 10.sp,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )

                        )

                        if (isCountdownOn) {
                            val animatedProgress by animateFloatAsState(
                                targetValue = countDownProgress.toFloat() / 60000,
                                animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                                label = ""
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                            ) {
                                CircularProgressIndicator(
                                    progress = animatedProgress,
                                    strokeWidth = 2.dp,
                                    color = Color(0xFFFFA500),
                                    modifier = Modifier
                                        .size(15.dp)
                                )
                                Text(
                                    text = "${countDownProgress / 1000}",
                                    color = Color.White,
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    ),
                                    fontSize = 8.sp,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                )
                            }

                        }
                        else {
                            val sensorStatusColor = when(connectionStatus) {
                                -1 -> Color.Red
                                0 -> Color.Red
                                1 -> Color(0xFFFFA500)
                                2 -> Color.Green
                                else -> Color.Gray
                            }
                            Surface(
                                color = sensorStatusColor,
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(15.dp)
                                    .align(Alignment.CenterVertically),
                                content = {}
                            )
                        }
                    }
                }
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


//https://towardsdev.com/jetpack-compose-custom-google-map-marker-erselan-khan-e6e04178a30b
@Composable
fun MapMarker(
    context: Context,
    position: LatLng,
    title: String,
    color: Color,
    @DrawableRes iconResourceId: Int
) {
    val icon = bitmapDescriptorFromVector(context, iconResourceId, color)
    Marker(
        state = MarkerState(position),
        title = title,
        icon = icon,
    )
}

fun bitmapDescriptorFromVector(
    context: Context,
    vectorResId: Int,
    color: Color
): BitmapDescriptor? {
    val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    drawable.colorFilter = BlendModeColorFilterCompat
        .createBlendModeColorFilterCompat(color.toArgb(), BlendModeCompat.SRC_ATOP)
    val bm = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )

    val canvas = android.graphics.Canvas(bm)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bm)
}

fun mapFloatToHue(value: Float): Float {
    return value * 130
}

@Composable
@Preview
fun StatusBarPreview() {
    val connectionStatus = 0
    val isCountdownOn = true
    val countDownProgress = 55000
    Row(
        modifier = Modifier
            .height(35.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = "Stav senzoru:",
            color = Color.White,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            )
        )

        val sensorStatusText = when(connectionStatus) {
            0 -> "Nepřipojen"
            1 -> "Připojování..."
            2 -> "Připojen"
            else -> "Chyba"
        }
        Text(
            text = sensorStatusText,
            color = Color.White,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 10.sp,
            style = TextStyle(
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            )

        )

        if (isCountdownOn) {
            val animatedProgress by animateFloatAsState(
                targetValue = countDownProgress.toFloat() / 60000,
                animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                label = ""
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
            ) {
                CircularProgressIndicator(
                    progress = animatedProgress,
                    strokeWidth = 2.dp,
                    color = Color(0xFFFFA500),
                    modifier = Modifier
                        .size(15.dp)
                )
                Text(
                    text = "${countDownProgress / 1000}",
                    color = Color.White,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    fontSize = 8.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }

        }
        else {
            val sensorStatusColor = when(connectionStatus) {
                -1 -> Color.Red
                0 -> Color.Red
                1 -> Color(0xFFFFA500)
                2 -> Color.Green
                else -> Color.Gray
            }
            Surface(
                color = sensorStatusColor,
                shape = CircleShape,
                modifier = Modifier
                    .size(15.dp)
                    .align(Alignment.CenterVertically),
                content = {}
            )
        }
    }
}