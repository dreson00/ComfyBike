@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)

package com.bk.bk1.compose

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
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
import androidx.compose.material3.Button
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
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
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.navigation.NavController
import com.bk.bk1.R
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
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.ui.IconGenerator
import com.leinardi.android.speeddial.compose.FabWithLabel
import com.leinardi.android.speeddial.compose.SpeedDial
import com.leinardi.android.speeddial.compose.SpeedDialState

@SuppressLint("SetTextI18n")
@Composable
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class,
    MapsComposeExperimentalApi::class, ExperimentalPermissionsApi::class,
    ExperimentalComposeUiApi::class
)
fun MainMapScreen(
    navController: NavController,
    viewModel: MainMapScreenViewModel,
    startSensorServiceWithAction: (String) -> Unit
) {
    val context = LocalContext.current
    var speedDialState by rememberSaveable { mutableStateOf(SpeedDialState.Collapsed) }
    var overlayVisible by rememberSaveable { mutableStateOf(speedDialState.isExpanded()) }
    val cameraPositionState = rememberCameraPositionState()
    val currentTrackId = viewModel.currentTrackId.observeAsState()

    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = getLocationPermissionList()
    )
    val showLocationPermissionRequestPreference by viewModel.showLocationPermissionRequest.observeAsState()
    val showLocationPermissionRequest = remember { mutableStateOf(false) }

    val bluetoothScanPermissionsState = rememberMultiplePermissionsState(
        permissions = getBluetoothPermissionList()
    )
    val showBluetoothScanRequest = remember { mutableStateOf(false) }

    val location: Location? by viewModel.location.observeAsState(null)
    val connectionStatus: Int by viewModel.connectionStatus.observeAsState(initial = 0)
    val isBluetoothAdapterOn: Boolean by viewModel.isBluetoothAdapterOn.observeAsState(initial = true)
    val isLocationEnabled: Boolean by viewModel.isLocationEnabled.observeAsState(initial = false)
    val trackingStatus: Int by viewModel.trackingStatus.observeAsState(initial = 0)
    val isCountdownOn: Boolean by viewModel.isCountdownOn.observeAsState(initial = false)
    val countDownProgress: Long by viewModel.countdownProgress.observeAsState(initial = 0L)

    LaunchedEffect(showLocationPermissionRequestPreference) {
        if (showLocationPermissionRequestPreference == "true" && !locationPermissionsState.allPermissionsGranted) {
            showLocationPermissionRequest.value = true
        }
    }

    if (showLocationPermissionRequest.value) {
        PermissionRequestDialog(
            messageText = stringResource(R.string.req_perm_loc),
            onConfirm = {
                locationPermissionsState.launchMultiplePermissionRequest()
                viewModel.enableCameraFollow()
            },
            onDismiss = {
                viewModel.disableLocationPermissionRequestDialog()
                showLocationPermissionRequest.value = false
            }
        )
    }

    if (showBluetoothScanRequest.value) {
        PermissionRequestDialog(
            messageText = stringResource(R.string.req_perm_scan),
            onConfirm = {
                bluetoothScanPermissionsState.launchMultiplePermissionRequest()
            },
            onDismiss = {
                showBluetoothScanRequest.value = false
            }
        )
    }

    if (locationPermissionsState.allPermissionsGranted && isLocationEnabled) {
        viewModel.enableLocationTracking()
    }

    LaunchedEffect(location, isLocationEnabled) {
        if (viewModel.cameraFollow && location != null && isLocationEnabled) {
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
                if (locationPermissionsState.allPermissionsGranted) {
                    if (connectionStatus == 0) {
                        item {
                            FabWithLabel(
                                onClick = {
                                    if (bluetoothScanPermissionsState.allPermissionsGranted) {
                                        if (isBluetoothAdapterOn) {
                                            navController.navigate("sensorConnectScreen")
                                        } else {
                                            Toast.makeText(
                                                context,
                                                context.getText(R.string.warn_bt_off),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    } else {
                                        showBluetoothScanRequest.value = true
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
                    if (isLocationEnabled && connectionStatus == 2 && trackingStatus == 0) {
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
                    } else if (trackingStatus == 1) {
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
                            speedDialState = SpeedDialState.Collapsed
                        }
                    }
                    val iconGenerator = remember { IconGenerator(context) }
                    val markerView = remember { LayoutInflater.from(context).inflate(R.layout.custom_marker_layout, null) }
                    val markerIconView = remember { markerView.findViewById<ImageView>(R.id.marker_icon) }
                    val markerLabel = remember { markerView.findViewById<TextView>(R.id.marker_label) }
                    markerIconView.setColorFilter(Red400.toArgb())
                    markerLabel.setTextColor(Red400.toArgb())
                    if (currentTrackId.value != null) {
                        val currentComfortIndexRecords = viewModel.currentComfortIndexRecordsFlow.collectAsState(
                            initial = emptyList()
                        )
                        if (currentComfortIndexRecords.value.isNotEmpty()) {
                            currentComfortIndexRecords.value.forEach { record ->
                                val color = Color.hsl(mapFloatToHue(record.comfortIndex), 1f, 0.5f)
                                MapMarker(
                                    context = LocalContext.current,
                                    position = LatLng(record.latitude, record.longitude),
                                    title = "${stringResource(R.string.ci_x)}${record.comfortIndex}",
                                    color = color,
                                    iconResourceId = R.drawable.baseline_circle_12
                                )
                            }
                        }
                    }

                    val firstComfortIndexRecordForAllTracks = viewModel
                        .firstComfortIndexRecordForAllExceptCurrent
                        .collectAsState(
                            initial = emptyList()
                    )
                    if (firstComfortIndexRecordForAllTracks.value.isNotEmpty()) {
                        firstComfortIndexRecordForAllTracks.value.forEach { record ->
                            markerIconView.setImageResource(R.drawable.bicycle_pin_48)
                            markerLabel.text = "${stringResource(R.string.label_track_x)} ${record.trackRecordId}"

                            iconGenerator.setBackground(null)
                            iconGenerator.setContentView(markerView)
                            MarkerInfoWindow(
                                state = rememberMarkerState(
                                    position = LatLng(record.latitude, record.longitude)
                                ),
                                icon = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon()),
                                onInfoWindowClick = { navController.navigate("trackDetailScreen/${record.trackRecordId }") },
                                onClick = {
                                    it.showInfoWindow()
                                    true
                                }
                            ) {
                                Box {
                                    Canvas(
                                        modifier = Modifier
                                            .width(105.dp)
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
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(25.dp))
                                            .padding(15.dp, 10.dp)
                                            .align(Alignment.Center)
                                    ) {
                                        Button(
                                            onClick = { }
                                        ) {
                                            Text(stringResource(R.string.btn_details))
                                        }
                                    }
                                }
                            }
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
                        if (!isLocationEnabled) {
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

                        val sensorStatusText = when(connectionStatus) {
                            0 -> stringResource(R.string.status_not_connected)
                            1 -> stringResource(R.string.status_connecting)
                            2 -> stringResource(R.string.status_connected)
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
                                content = { }
                            )
                        }
                    }
                }
                if (isLocationEnabled) {
                    FloatingActionButton(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        onClick = {
                            viewModel.toggleCameraFollow()
                        }
                    ) {
                        if (viewModel.cameraFollow) {
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
                        modifier = Modifier
                            .align(Alignment.BottomStart)
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
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        onClick = {
                            showLocationPermissionRequest.value = true
                        }
                    ) {
                        Icon(Icons.Outlined.Warning,
                            contentDescription = stringResource(R.string.desc_perm_required)
                        )
                    }
                }
            }
        }
    )
}

@Composable
@Preview
fun MarkerWindowPreview() {
    Box {
        Canvas(
            modifier = Modifier
                .width(105.dp)
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(25.dp))
                .padding(15.dp, 10.dp)
                .align(Alignment.Center)
        ) {
            Button(
                onClick = { },

            ) {
                Text("Detaily")
            }
        }
    }
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
        state = rememberMarkerState(position = position),
        title = title,
        icon = icon,
        onClick = {
            it.showInfoWindow()
            true
        }
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