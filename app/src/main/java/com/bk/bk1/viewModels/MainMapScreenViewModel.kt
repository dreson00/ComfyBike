package com.bk.bk1.viewModels


import android.location.Location
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bk.bk1.MapState
import com.google.android.gms.location.FusedLocationProviderClient

class MainMapScreenViewModel(val fLocationProviderClient: FusedLocationProviderClient) : ViewModel() {

    val mapState: MutableState<MapState> = mutableStateOf(
        MapState(
            lastKnownLocation = null,
            cameraFollow = true
        )
    )

    val lastKnownLocation: MutableState<Location?> = mutableStateOf(null)
    var cameraFollow by mutableStateOf(true)

    init {
        getDeviceLocation()
    }

    fun toggleCameraFollow() {
        if (cameraFollow) {
            disableCameraFollow()
        }
        else {
            enableCameraFollow()
        }
    }

    fun enableCameraFollow() {
        cameraFollow = true
    }

    fun disableCameraFollow() {
        cameraFollow = false
    }

    private fun getDeviceLocation() {
        try {
            val locationResult = fLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    lastKnownLocation.value = task.result
                }
            }
        } catch (e: SecurityException) {
            //
        }
    }

}