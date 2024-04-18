package com.bk.bk1.compose

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bk.bk1.R
import com.bk.bk1.enums.ImageSavingStatus
import com.bk.bk1.ui.theme.DimGreen
import com.bk.bk1.ui.theme.Red700
import com.bk.bk1.viewModels.MapScreenshotterScreenViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.smarttoolfactory.screenshot.ScreenshotBox
import com.smarttoolfactory.screenshot.rememberScreenshotState

@Composable
fun MapScreenshotterScreen(
    viewModel: MapScreenshotterScreenViewModel,
    navController: NavController,
    trackId: Int?
) {
    val state by viewModel.state.collectAsState()
    val screenshotState = rememberScreenshotState()
    val context = LocalContext.current

    if (screenshotState.bitmap != null && trackId != null) {
        LaunchedEffect(Unit) {
            viewModel.setImageSavingStatus(ImageSavingStatus.PROCESSING)
            viewModel.saveImage(screenshotState.bitmap!!, trackId)
        }
    }

    if (state.imageSavingStatus == ImageSavingStatus.SUCCESS) {
        LaunchedEffect(Unit) {
            Toast.makeText(
                context,
                context.getText(R.string.info_img_saved),
                Toast.LENGTH_LONG
            ).show()
            navController.popBackStack()
        }
    }
    else if (state.imageSavingStatus == ImageSavingStatus.ERROR) {
        LaunchedEffect(Unit) {
            Toast.makeText(
                context,
                context.getText(R.string.info_exp_err),
                Toast.LENGTH_LONG
            ).show()
            navController.popBackStack()
        }
    }

    ScreenshotBox(
        modifier = Modifier
            .fillMaxSize(),
        screenshotState = screenshotState
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val cameraPositionState = rememberCameraPositionState()
            trackId?.let {
                LaunchedEffect(Unit) {
                    viewModel.initTrackData(it)
                }
                val comfortIndexRecords = state.comfortIndexRecords

                if (comfortIndexRecords.isNotEmpty()) {
                    val middleRecord = comfortIndexRecords[comfortIndexRecords.count() / 2]
                    LaunchedEffect(Unit) {
                        cameraPositionState.animate(CameraUpdateFactory.zoomTo(17F))
                        cameraPositionState.animate(
                            CameraUpdateFactory
                                .newLatLng(LatLng(middleRecord.latitude, middleRecord.longitude))
                        )
                    }
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        properties = MapProperties(
                            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                                context,
                                R.raw.map_style_no_poi
                            )
                        ),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                            myLocationButtonEnabled = false
                        ),
                        cameraPositionState = cameraPositionState,
                    ) {
                        if (state.comfortIndexRecords.isNotEmpty()) {
                            state.comfortIndexRecords.forEach { record ->
                                UnoptimizedComfortIndexRecordMapMarker(record)
                            }
                        }
                    }
                }
            }

            if (state.showSpeedFilterWatermark) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(color = MaterialTheme.colorScheme.background)
                ) {
                    Text(
                        modifier = Modifier
                            .padding(5.dp),
                        text = "${stringResource(R.string.label_speed_range_x)} %.2f km/h – %.2f km/h"
                            .format(state.speedFilterRange.start, state.speedFilterRange.endInclusive)
                    )
                }
            }


            if (state.showSettings && !state.hideUI) {
                SettingsCard(viewModel)
            }

            if (state.imageSavingStatus == ImageSavingStatus.PROCESSING) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.5f)
                        .align(Alignment.Center),
                ) { }
                CircularProgressIndicator(
                    modifier = Modifier
                        .width(64.dp)
                        .align(Alignment.Center),
                )
            }

            if (!state.hideUI) {
                ButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp),
                    navController,
                    viewModel
                )
            }

            if (state.beginCapture) {
                LaunchedEffect(Unit) {
                    screenshotState.capture()
                }
            }
        }
    }
}

@Composable
fun SettingsCard(
    viewModel: MapScreenshotterScreenViewModel
) {
    val state by viewModel.state.collectAsState()
    ElevatedCard(
        modifier = Modifier
            .padding(10.dp),
        colors = CardDefaults
            .elevatedCardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .padding(10.dp)
            ) {
                SpeedFilterSlider(
                    speedRange = state.speedMin..state.speedMax
                ) {
                    viewModel.filterRecordsBySpeedRange(it)
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .toggleable(
                        value = state.showSpeedFilterWatermark,
                        onValueChange = {
                            viewModel.toggleSpeedFilterWatermark()
                        }
                    )
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = state.showSpeedFilterWatermark,
                    onCheckedChange = null
                )
                Text(
                    text = stringResource(R.string.label_add_speed_filter_watermark),
                    color = Color.Black,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            ElevatedButton(
                onClick = {
                    viewModel.toggleSettingsVisible()
                },
                colors = ButtonDefaults
                    .elevatedButtonColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Icon(
                    Icons.Rounded.Close,
                    stringResource(R.string.btn_cancel),
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
fun ButtonRow(
    modifier: Modifier,
    navController: NavController,
    viewModel: MapScreenshotterScreenViewModel
) {
    val state by viewModel.state.collectAsState()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            modifier = Modifier
                .size(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DimGreen),
            contentPadding = PaddingValues(0.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp),
            shape = RoundedCornerShape(30.dp),
            enabled = !state.hideUI,
            onClick = {
                viewModel.hideUI()
                viewModel.beginCapture()
            }
        ) {
            Icon(
                painterResource(R.drawable.baseline_save_24),
                "",
                tint = Color.Black
            )
        }

        if (state.speedMin != state.speedMax) {
            Button(
                modifier = Modifier
                    .size(40.dp),
                contentPadding = PaddingValues(0.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp),
                shape = RoundedCornerShape(30.dp),
                enabled = !state.hideUI,
                onClick = {
                    viewModel.toggleSettingsVisible()
                }
            ) {
                Icon(
                    Icons.Rounded.Settings,
                    ""
                )
            }
        }
        Button(
            modifier = Modifier
                .size(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Red700),
            contentPadding = PaddingValues(0.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp),
            shape = RoundedCornerShape(30.dp),
            enabled = !state.hideUI,
            onClick = {
                navController.popBackStack()
            }
        ) {
            Icon(
                Icons.Rounded.Close,
                ""
            )
        }
    }
}

@Preview
@Composable
fun MapScreenshotterScreenPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val cameraPositionState = rememberCameraPositionState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Blue)
                .align(Alignment.Center),
        ) {}

        val (checkedState, onStateChange) = remember { mutableStateOf(true) }
        ElevatedCard(
            modifier = Modifier
                .padding(10.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .padding(10.dp)
                ) {
//                    SpeedFilterSlider(
//                        0f..30f
//                    ) { }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .toggleable(
                            value = checkedState,
                            onValueChange = { onStateChange(!checkedState) },
                        )
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checkedState,
                        onCheckedChange = null
                    )
                    Text(
                        text = "Option selection",
                        color = Color.Black,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                ElevatedButton(
                    onClick = { /*TODO*/ },

                    ) {
                    Icon(
                        Icons.Rounded.Close,
                        stringResource(R.string.btn_cancel),
                        tint = Color.Black
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                modifier = Modifier
                    .size(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DimGreen),
                contentPadding = PaddingValues(0.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp),
                onClick = {},
            ) {
                Icon(
                    painterResource(R.drawable.baseline_save_24),
                    "",
                    tint = Color.Black
                )
            }
            FloatingActionButton(
                modifier = Modifier
                    .size(40.dp),
                shape = RoundedCornerShape(30.dp),
                onClick = {}
            ) {
                Icon(
                    Icons.Rounded.Settings,
                    ""
                )
            }
            FloatingActionButton(
                modifier = Modifier
                    .size(60.dp),
                containerColor = Red700,
                shape = RoundedCornerShape(30.dp),
                onClick = {}
            ) {
                Icon(
                    Icons.Rounded.Close,
                    ""
                )
            }
        }
    }
}