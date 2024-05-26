@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)

package com.bk.bk1.compose

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bk.bk1.R
import com.bk.bk1.enums.SensorConnectionStatus
import com.bk.bk1.enums.TrackingStatus
import com.bk.bk1.models.ComfortIndexRecord
import com.bk.bk1.states.MainMapScreenState
import com.bk.bk1.ui.theme.BK1Theme
import com.bk.bk1.ui.theme.DimGreen
import com.bk.bk1.ui.theme.GreyLightBlue
import com.bk.bk1.ui.theme.Red400
import com.bk.bk1.ui.theme.Red700
import com.bk.bk1.utilities.SensorService
import com.bk.bk1.utilities.getBluetoothPermissionList
import com.bk.bk1.utilities.getLocationPermissionList
import com.bk.bk1.viewModels.MainMapScreenViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.ui.IconGenerator
import com.leinardi.android.speeddial.compose.FabWithLabel
import com.leinardi.android.speeddial.compose.SpeedDial
import com.leinardi.android.speeddial.compose.SpeedDialState

@SuppressLint("SetTextI18n")
@Composable
@OptIn(ExperimentalPermissionsApi::class
)
fun MainMapScreen(
    navController: NavController,
    viewModel: MainMapScreenViewModel,
    startSensorServiceWithAction: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = getLocationPermissionList()
    )
    val bluetoothScanPermissionsState = rememberMultiplePermissionsState(
        permissions = getBluetoothPermissionList()
    )

    LaunchedEffect(state.showLocationPermissionRequestPreference) {
        if (state.showLocationPermissionRequestPreference == "true" && !locationPermissionsState.allPermissionsGranted) {
            viewModel.setShowLocationPermissionRequest(true)
        }
    }

    if (state.showLocationPermissionRequest) {
        PermissionRequestDialog(
            messageText = stringResource(R.string.req_perm_loc),
            onConfirm = {
                locationPermissionsState.launchMultiplePermissionRequest()
                viewModel.enableCameraFollow()
            },
            onDismiss = {
                viewModel.disableLocationPermissionRequestDialog()
                viewModel.setShowLocationPermissionRequest(false)
            }
        )
    }

    if (state.showBluetoothScanRequest) {
        PermissionRequestDialog(
            messageText = stringResource(R.string.req_perm_scan),
            onConfirm = {
                bluetoothScanPermissionsState.launchMultiplePermissionRequest()
            },
            onDismiss = {
                viewModel.setShowBluetoothScanRequest(false)
            }
        )
    }

    if (locationPermissionsState.allPermissionsGranted && state.isLocationEnabled) {
        viewModel.enableLocationTracking()
    }

    LaunchedEffect(state.location, state.isLocationEnabled) {
        if (state.cameraFollow && state.location != null && state.isLocationEnabled) {
            cameraPositionState.animate(
                CameraUpdateFactory
                    .newLatLng(LatLng(state.location!!.latitude, state.location!!.longitude))
            )
            if (cameraPositionState.position.zoom < 15F) {
                cameraPositionState.animate(CameraUpdateFactory.zoomTo(17F))
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            SpeedDialButtonMenu(
                state = state,
                viewModel = viewModel,
                navController = navController,
                locationPermissionsState = locationPermissionsState,
                bluetoothScanPermissionsState = bluetoothScanPermissionsState,
                startSensorServiceWithAction = startSensorServiceWithAction
            )
        },
        content = { padding ->
            Box(modifier = Modifier.padding(padding)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
                    properties = MapProperties(
                        isMyLocationEnabled = locationPermissionsState.allPermissionsGranted,
                        mapStyleOptions =  MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_no_poi)
                    ),
                    cameraPositionState = cameraPositionState,
                ) {
                    LaunchedEffect(cameraPositionState.isMoving) {
                        if (cameraPositionState.isMoving && cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE) {
                            viewModel.disableCameraFollow()
                            viewModel.setSpeedDialMenuState(SpeedDialState.Collapsed)
                        }
                    }

                    if (state.currentTrackId != null) {
                        if (state.currentComfortIndexRecords.isNotEmpty()) {
                            state.currentComfortIndexRecords.forEach { record ->
                                ComfortIndexRecordMapMarker(record)
                            }
                        }
                    }

                    val iconGenerator = remember { IconGenerator(context) }
                    val markerView = remember { LayoutInflater.from(context).inflate(R.layout.custom_marker_layout, null) }
                    val markerIconView = remember { markerView.findViewById<ImageView>(R.id.marker_icon) }
                    val markerLabel = remember { markerView.findViewById<TextView>(R.id.marker_label) }
                    markerIconView.setColorFilter(Red400.toArgb())
                    markerLabel.setTextColor(Red400.toArgb())

                    if (state.firstComfortIndexRecordForAllTracks.isNotEmpty()) {
                        state.firstComfortIndexRecordForAllTracks.forEach { record ->
                            TrackMarker(
                                record = record,
                                navController = navController,
                                markerView = markerView,
                                markerIconView = markerIconView,
                                markerLabel = markerLabel,
                                iconGenerator = iconGenerator
                            )
                        }
                    }
                }

                SensorStatusBar(state)

                FollowLocationButton(
                    state = state,
                    modifier = Modifier.align(Alignment.BottomStart),
                    viewModel = viewModel,
                    locationPermissionsState = locationPermissionsState,
                )

                if (state.trackingStatus == TrackingStatus.TRACKING) {
                    TrackingStatusBar(modifier = Modifier.align(Alignment.BottomCenter))
                }
            }
        }
    )
}

@Composable
fun TrackingStatusBar(
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black)

    ) {
        Text(
            text = stringResource(R.string.label_tracking_on),
            color = DimGreen,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(10.dp)
        )
    }
}

@Composable
fun FollowLocationButton(
    state: MainMapScreenState,
    modifier: Modifier,
    viewModel: MainMapScreenViewModel,
    locationPermissionsState: MultiplePermissionsState,
) {
    val context = LocalContext.current
    if (state.isLocationEnabled) {
        FloatingActionButton(
            modifier = modifier
                .padding(16.dp),
            onClick = {
                viewModel.toggleCameraFollow()
            }
        ) {
            if (state.cameraFollow) {
                Icon(
                    painterResource(
                        R.drawable.baseline_my_location_24
                    ),
                    contentDescription = stringResource(R.string.desc_my_loc)
                )
            } else {
                Icon(
                    painterResource(
                        R.drawable.baseline_location_searching_24
                    ),
                    contentDescription = stringResource(R.string.desc_my_loc)
                )
            }
        }
    }
    else {
        FloatingActionButton(
            modifier = modifier
                .padding(16.dp),
            containerColor = Red700,
            onClick = {
                Toast.makeText(
                    context,
                    context.getText(R.string.warn_loc_off),
                    Toast.LENGTH_LONG
                ).show()
            }
        ) {
            Icon(
                painterResource(
                    R.drawable.baseline_location_off_24
                ),
                contentDescription = stringResource(R.string.warn_loc_off),
            )
        }
    }
    if (!locationPermissionsState.allPermissionsGranted) {
        FloatingActionButton(
            containerColor = Red700,
            modifier = modifier
                .padding(16.dp),
            onClick = {
                viewModel.setShowLocationPermissionRequest(true)
            }
        ) {
            Icon(Icons.Outlined.Warning,
                contentDescription = stringResource(R.string.desc_perm_required)
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalPermissionsApi::class)
@Composable
fun SpeedDialButtonMenu(
    state: MainMapScreenState,
    viewModel: MainMapScreenViewModel,
    navController: NavController,
    locationPermissionsState: MultiplePermissionsState,
    bluetoothScanPermissionsState: MultiplePermissionsState,
    startSensorServiceWithAction: (String) -> Unit
) {
    val context = LocalContext.current
    SpeedDial(
        fabClosedContent = { Icon(Icons.Filled.Menu, null) },
        state = state.speedDialState,
        onFabClick = { expanded ->
            viewModel.setSpeedDialOverlayVisible(!expanded)
            viewModel.toggleSpeedDialMenuState()
        }
    ) {
        if (locationPermissionsState.allPermissionsGranted) {
            if (state.connectionStatus == SensorConnectionStatus.DISCONNECTED) {
                item {
                    FabWithLabel(
                        onClick = {
                            if (bluetoothScanPermissionsState.allPermissionsGranted) {
                                if (state.isBluetoothAdapterOn) {
                                    navController.navigate("sensorConnectScreen")
                                } else {
                                    Toast.makeText(
                                        context,
                                        context.getText(R.string.warn_bt_off),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                viewModel.setShowBluetoothScanRequest(true)
                            }
                        },
                        labelContent = {
                            Text(text = stringResource(R.string.btn_sensor_connect))
                        }
                    ) {
                        Icon(Icons.Default.Add, null)
                    }
                }
            } else {
                item {
                    FabWithLabel(
                        fabContainerColor = Red400,
                        onClick = {
                            startSensorServiceWithAction(SensorService.Actions.STOP_ALL.toString())
                        },
                        labelContent = {
                            Text(
                                text = stringResource(R.string.btn_sensor_disconnect),
                                color = Red700
                            )
                        },
                    ) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            }
            if (state.isLocationEnabled
                && state.connectionStatus == SensorConnectionStatus.CONNECTED
                && state.trackingStatus == TrackingStatus.NOT_TRACKING
                ) {
                item {
                    FabWithLabel(
                        onClick = {
                            startSensorServiceWithAction(SensorService.Actions.START_TRACKING.toString())
                        },
                        labelContent = { Text(text = stringResource(R.string.btn_new_track_start)) },
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                    }
                }
            } else if (state.trackingStatus == TrackingStatus.TRACKING) {
                item {
                    FabWithLabel(
                        onClick = {
                            startSensorServiceWithAction(SensorService.Actions.STOP_TRACKING.toString())
                        },
                        labelContent = { Text(text = stringResource(R.string.btn_new_track_stop)) },
                    ) {
                        Icon(
                            painterResource(R.drawable.baseline_stop_24),
                            null
                        )
                    }
                }
            }
        }
        item {
            FabWithLabel(
                onClick = { navController.navigate("trackList") },
                labelContent = { Text(text = stringResource(R.string.btn_track_list)) },
            ) {
                Icon(Icons.AutoMirrored.Filled.List, null)
            }
        }
    }
}



@Composable
fun SensorStatusBar(
    state: MainMapScreenState
) {
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
            if (!state.isLocationEnabled) {
                Icon(
                    painterResource(R.drawable.baseline_location_off_24),
                    contentDescription = stringResource(R.string.warn_loc_off),
                    tint = Red700
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(color = Color.Gray)
                )
            }
            Text(
                text = stringResource(R.string.status_text),
                color = Color.White,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )

            val sensorStatusText = when(state.connectionStatus) {
                SensorConnectionStatus.DISCONNECTED -> stringResource(R.string.status_not_connected)
                SensorConnectionStatus.CONNECTING -> stringResource(R.string.status_connecting)
                SensorConnectionStatus.CONNECTED -> stringResource(R.string.status_connected)
                else -> stringResource(R.string.status_error)
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

            if (state.isCountdownOn) {
                val animatedProgress by animateFloatAsState(
                    targetValue = state.countdownProgress.toFloat() / 60000,
                    animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                    label = ""
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                ) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .size(15.dp),
                        color = Color(0xFFFFA500),
                        strokeWidth = 2.dp,
                    )
                    Text(
                        text = "${state.countdownProgress / 1000}",
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
                val sensorStatusColor = when(state.connectionStatus) {
                    SensorConnectionStatus.ERROR -> Color.Red
                    SensorConnectionStatus.DISCONNECTED -> Color.Red
                    SensorConnectionStatus.CONNECTING -> Color(0xFFFFA500)
                    SensorConnectionStatus.CONNECTED -> Color.Green
                }
                Surface(
                    color = sensorStatusColor,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(15.dp)
                        .align(Alignment.CenterVertically),
                    content = { }
                )
            }
        }
    }
}

@Composable
@Preview
fun MarkerWindowPreview() {
    val record = ComfortIndexRecord(
        0,
        0.5f,
        15f,
        2,
        0.0,
        0.0
    )
    Box {
        Canvas(
            modifier = Modifier
                .width(300.dp)
                .height(50.dp)
                .align(Alignment.Center)
        ) {
            val trianglePath = Path().let {
                it.moveTo(this.size.width * .40f, this.size.height - 2f)
                it.lineTo(this.size.width * .50f, this.size.height + 30f)
                it.lineTo(this.size.width * .60f, this.size.height - 2f)
                it.close()
                it
            }
            drawRoundRect(
                GreyLightBlue,
                size = Size(this.size.width, this.size.height),
                cornerRadius = CornerRadius(60f)
            )
            drawPath(
                path = trianglePath,
                GreyLightBlue,
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(25.dp))
                .padding(15.dp, 10.dp)
                .align(Alignment.Center)
        ) {
            Text("${stringResource(R.string.ci_x)} ${record.comfortIndex}")
            Text("${stringResource(R.string.speed_x)} ${record.bicycleSpeed}")
        }
    }
}



@Composable
//@Preview
fun StatusBarPreview() {
    val connectionStatus = 0
    val isCountdownOn = true
    val countDownProgress = 55000
    val isLocationEnabled = false
    Row(
        modifier = Modifier
            .height(35.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        if (!isLocationEnabled) {
            Icon(
                painterResource(R.drawable.baseline_location_off_24),
                contentDescription = "Zjišťování polohy není povoleno",
                tint = Red700
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(color = Color.Gray)
            )
        }
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
                    progress = { animatedProgress },
                    modifier = Modifier
                        .size(15.dp),
                    color = Color(0xFFFFA500),
                    strokeWidth = 2.dp,
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

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
//@Preview(apiLevel = Build.VERSION_CODES.R, showSystemUi = true)
fun MainMapScreenPreview() {
    BK1Theme(darkTheme = false) {
        val context = LocalContext.current
        var speedDialState by rememberSaveable { mutableStateOf(SpeedDialState.Expanded) }
        var overlayVisible by rememberSaveable { mutableStateOf(speedDialState.isExpanded()) }
        val connectionStatus = 2
        val trackingStatus = 1
        val isCountdownOn = false
        val countDownProgress = 100000

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
                                fabContainerColor = DimGreen,
                                onClick = {
                                },
                                labelContent = { Text(text = "Připojit senzor") },
                            ) {
                                Icon(Icons.Default.Add, null)
                            }
                        }
                    } else {
                        item {
                            FabWithLabel(
                                fabContainerColor = Red400,
                                onClick = { },
                                labelContent = {
                                    Text(
                                        text = "Odpojit senzor",
                                        color = Red700
                                    )
                                },
                            ) {
                                Icon(Icons.Default.Close, null)
                            }
                        }
                    }
                    if (connectionStatus == 2 && trackingStatus == 0) {
                        item {
                            FabWithLabel(
                                onClick = { },
                                labelContent = { Text(text = "Zaznamenat novou trasu") },
                            ) {
                                Icon(Icons.Default.PlayArrow, null)
                            }
                        }
                    } else if (trackingStatus == 1) {
                        item {
                            FabWithLabel(
                                onClick = { },
                                labelContent = { Text(text = "Ukončit zaznamenávání") },
                            ) {
                                Icon(
                                     painterResource(R.drawable.baseline_stop_24),
                                    null
                                )
                            }
                        }
                    }
//                    item {
//                        FabWithLabel(
//                            onClick = { },
//                            labelContent = { Text(text = "Smazat všechny trasy") },
//                        ) {
//                            Icon(Icons.Default.Delete, null)
//                        }
//                    }
                    item {
                        FabWithLabel(
                            onClick = { },
                            labelContent = { Text(text = "Spravovat trasy") },
                        ) {
                            Icon(Icons.AutoMirrored.Filled.List, null)
                        }
                    }
                }
            },
            content = { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                            myLocationButtonEnabled = false
                        ),
                        properties = MapProperties(isMyLocationEnabled = true),
                        cameraPositionState = rememberCameraPositionState()
                    )
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

                            val sensorStatusText = when (connectionStatus) {
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

                            } else {
                                val sensorStatusColor = when (connectionStatus) {
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
                        onClick = { }
                    ) {
                        if (true) {
                            Icon(painterResource(
                                R.drawable.baseline_my_location_24),
                                contentDescription = "Moje poloha"
                            )
                        } else {
                            Icon(painterResource(
                                R.drawable.baseline_location_searching_24),
                                contentDescription = "Moje poloha"
                            )
                        }
                    }
                }
            }
        )
    }
}