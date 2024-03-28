package com.bk.bk1.utilities

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class DefaultLocationClient(
    private val context: Context,
    private val client: FusedLocationProviderClient
): LocationClient {
    private var isReceivingLocationUpdates = false
    private lateinit var locationCallback: LocationCallback
    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long): Flow<Location> {
        return callbackFlow {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!isGpsEnabled && !isNetworkEnabled) {
                throw LocationClient.LocationException("GPS is disabled")
            }

            val locationRequest: LocationRequest =
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval).apply {
                    setWaitForAccurateLocation(true)
                }.build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    locationResult.locations.lastOrNull()?.let { location ->
                        launch {
                            send(location)
                        }
                    }
                }
            }

            LocationManager.MODE_CHANGED_ACTION

            client.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            isReceivingLocationUpdates = true

            awaitClose {
                isReceivingLocationUpdates = false
                removeLocationUpdates()
            }
        }
    }

    override fun isReceivingLocationUpdates(): Boolean {
        return isReceivingLocationUpdates
    }

    override fun removeLocationUpdates() {
        isReceivingLocationUpdates = false
        client.removeLocationUpdates(locationCallback)
    }

}