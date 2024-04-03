package com.bk.bk1.compose

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.bk.bk1.R
import com.bk.bk1.viewModels.MapScreenshotterScreenViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.rememberCameraPositionState
import com.smarttoolfactory.screenshot.ScreenshotBox
import com.smarttoolfactory.screenshot.rememberScreenshotState

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun MapScreenshotterScreen(
    viewModel: MapScreenshotterScreenViewModel,
    navController: NavController,
    trackId: Int?
) {
    val screenshotState = rememberScreenshotState()
    val context = LocalContext.current

    if (screenshotState.bitmap != null && trackId != null) {
        LaunchedEffect(Unit) {
            if (viewModel.saveImage(screenshotState.bitmap!!, trackId) == 0) {
                Toast.makeText(
                    context,
                    context.getText(R.string.info_img_saved),
                    Toast.LENGTH_LONG
                ).show()
                navController.popBackStack()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        ScreenshotBox(screenshotState = screenshotState) {
            val cameraPositionState = rememberCameraPositionState()

            trackId?.let {
                val comfortIndexRecords = viewModel
                    .getComfortIndexRecordsByTrackId(it)
                    .collectAsState(emptyList())

                if (comfortIndexRecords.value.isNotEmpty()) {
                    val recordsValue = comfortIndexRecords.value
                    val middleRecord = recordsValue[recordsValue.count() / 2]
                    LaunchedEffect(comfortIndexRecords.value.first()) {
                        cameraPositionState.animate(CameraUpdateFactory.zoomTo(17F))
                        cameraPositionState.animate(
                            CameraUpdateFactory
                                .newLatLng(LatLng(middleRecord.latitude, middleRecord.longitude))
                        )
                    }
                    GoogleMap(
                        modifier = Modifier.fillMaxHeight(0.92f),
                        uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
                        cameraPositionState = cameraPositionState,
                    ) {
                        MapEffect { map ->
                            map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_no_poi))
                        }
                        comfortIndexRecords.value.forEach { item ->
                            val color = Color.hsl(mapFloatToHue(item.comfortIndex), 1f, 0.5f)
                            MapMarker(
                                context = LocalContext.current,
                                position = LatLng(item.latitude, item.longitude),
                                title = "${stringResource(R.string.label_comfort_index)}${item.comfortIndex}",
                                color = color,
                                iconResourceId = R.drawable.baseline_circle_12
                            )
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            Button(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                shape = RectangleShape,
                onClick = {
                    screenshotState.capture()
                }
            ) {
                Text(stringResource(R.string.btn_save))
            }
            Button(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RectangleShape,
                onClick = {
                    navController.popBackStack()
                }
            ) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
//        screenshotState.imageBitmap?.let {
//            Image(
//                modifier = Modifier
//                    .width(200.dp)
//                    .height(150.dp),
//                bitmap = it,
//                contentDescription = null
//            )
//        }
    }
}

@Preview
@Composable
fun MapScreenshotterScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        val cameraPositionState = rememberCameraPositionState()
        GoogleMap(
            modifier = Modifier.fillMaxHeight(0.92f),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            ),
            properties = MapProperties(isMyLocationEnabled = true),
            cameraPositionState = cameraPositionState
        )
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            Button(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                shape = RectangleShape,
                onClick = {
                }
            ) {
                Text("Uložit")
            }
            Button(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RectangleShape,
                onClick = {
                }
            ) {
                Text("Zrušit")
            }
        }
    }
}