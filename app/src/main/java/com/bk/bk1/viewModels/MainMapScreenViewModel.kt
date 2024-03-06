package com.bk.bk1.viewModels


import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority

@SuppressLint("MissingPermission")
class MainMapScreenViewModel(
    private val fLocationProviderClient: FusedLocationProviderClient,
) : ViewModel() {

    var cameraFollow by mutableStateOf(true)
    var location = MutableLiveData<Location>(null)

    private var locationRequest: LocationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).apply {
            setWaitForAccurateLocation(true)
        }.build()
    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            location.postValue(locationResult.lastLocation)
        }
    }


    init {
        fLocationProviderClient
            .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
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

    override fun onCleared() {
        super.onCleared()
        fLocationProviderClient.removeLocationUpdates(locationCallback)
    }

}