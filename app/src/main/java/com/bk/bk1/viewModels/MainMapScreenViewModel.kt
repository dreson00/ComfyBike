package com.bk.bk1.viewModels


import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.bk.bk1.MapState
import com.google.android.gms.location.FusedLocationProviderClient
import java.util.Timer
import java.util.TimerTask

class MainMapScreenViewModel(val fLocationProviderClient: FusedLocationProviderClient) : ViewModel() {

    private val timer = Timer()
    val mapState: MutableState<MapState> = mutableStateOf(
        MapState(
            lastKnownLocation = null,
        )
    )

    init {
        timer.schedule(object : TimerTask() {
            override fun run() {
                getDeviceLocation()
            }
        }, 0, 1000)
    }

    fun getDeviceLocation() {
        try {
            val locationResult = fLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mapState.value = mapState.value.copy(
                        lastKnownLocation = task.result,
                    )
                }
            }
        } catch (e: SecurityException) {
            //
        }
    }

}