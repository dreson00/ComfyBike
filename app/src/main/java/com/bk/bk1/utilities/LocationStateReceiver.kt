package com.bk.bk1.utilities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import javax.inject.Inject

class LocationStateReceiver @Inject constructor(
    private val locationManager: LocationManager,
    private val locationStateUpdater: LocationStateUpdater
) : BroadcastReceiver() {
    init {
        locationStateUpdater.updateLocationState(getState())
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        if (LocationManager.PROVIDERS_CHANGED_ACTION == intent?.action) {
            locationStateUpdater.updateLocationState(getState())
        }
    }

    private fun getState(): Boolean {
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return isGpsEnabled || isNetworkEnabled
    }
}