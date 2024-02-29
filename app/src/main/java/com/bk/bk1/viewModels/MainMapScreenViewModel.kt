package com.bk.bk1.viewModels


import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bk.bk1.models.BluetoothDeviceInfo
import com.bk.bk1.utilities.SensorConnectionListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.movesense.mds.Mds

@SuppressLint("MissingPermission")
class MainMapScreenViewModel(
    private val fLocationProviderClient: FusedLocationProviderClient,
    private val mds: Mds
) : ViewModel() {
    var cameraFollow by mutableStateOf(true)
    val location = mutableStateOf<Location?>(null)

    private var locationRequest: LocationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).apply {
            setWaitForAccurateLocation(true)
        }.build()
    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            location.value = locationResult.lastLocation
        }
    }

    var deviceInfo = BluetoothDeviceInfo(
        address = "",
        MutableLiveData(""),
        MutableLiveData(0))

    init {
        fLocationProviderClient
            .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun connectToSensor(sensorAddress: String) {
        deviceInfo = BluetoothDeviceInfo(
            address = sensorAddress,
            MutableLiveData(String()),
            MutableLiveData(0)
        )
        mds.connect(sensorAddress, SensorConnectionListener(deviceInfo))
    }

    fun disconnectSensor() {
        deviceInfo.address?.let {
            mds.disconnect(it)
            deviceInfo.address = ""
        }
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