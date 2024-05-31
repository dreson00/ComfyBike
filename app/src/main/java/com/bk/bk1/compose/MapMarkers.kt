package com.bk.bk1.compose

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.navigation.NavController
import com.bk.bk1.R
import com.bk.bk1.models.ComfortIndexRecord
import com.bk.bk1.ui.theme.GreyLightBlue
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.ui.IconGenerator

@SuppressLint("SetTextI18n")
@Composable
fun TrackMarker(
    record: ComfortIndexRecord,
    navController: NavController,
    markerView: View,
    markerIconView: ImageView,
    markerLabel: TextView,
    iconGenerator: IconGenerator
) {
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

@Composable
fun ComfortIndexRecordMapMarker(
    record: ComfortIndexRecord
) {
    MarkerInfoWindow(
        state = rememberMarkerState(
            position = LatLng(record.latitude, record.longitude)
        ),
        icon = bitmapDescriptorFromVector(
            LocalContext.current,
            R.drawable.baseline_circle_12,
            getColorFromCiRecord(record)
        ),
        onClick = { marker ->
            marker.showInfoWindow()
            true
        }
    ) {
        ComfortIndexRecordMapMarkerWindow(record)
    }
}

@Composable
fun UnoptimizedComfortIndexRecordMapMarker(
    record: ComfortIndexRecord,
) {
    MarkerInfoWindow(
        state = MarkerState(LatLng(record.latitude, record.longitude)),
        icon = bitmapDescriptorFromVector(
            LocalContext.current,
            R.drawable.baseline_circle_12,
            getColorFromCiRecord(record)
        ),
        onClick = { marker ->
            marker.showInfoWindow()
            true
        }
    ) {
        ComfortIndexRecordMapMarkerWindow(record)
    }
}

@Composable
fun ComfortIndexRecordMapMarkerWindow(record: ComfortIndexRecord) {
    Box {
        Canvas(
            modifier = Modifier
                .width(350.dp)
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
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(25.dp))
                .padding(15.dp, 10.dp)
                .align(Alignment.Center)
        ) {
            Text(
                text = "${stringResource(R.string.ci_x)} ${record.comfortIndex}",
                color = Color.Black
            )
            Text(
                text = "${stringResource(R.string.speed_x)} ${"%.2f km/h".format(record.bicycleSpeed)}",
                color = Color.Black
            )
        }
    }
}

fun getColorFromCiRecord(record: ComfortIndexRecord): Color {
    val hue = 100 * record.comfortIndex
    return Color.hsl(hue, 1f, 0.5f)
}

//https://towardsdev.com/jetpack-compose-custom-google-map-marker-erselan-khan-e6e04178a30b
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