package com.bk.bk1.utilities

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationClient {
    fun getLocationUpdates(interval: Long): Flow<Location>

    fun removeLocationUpdates()

    fun isReceivingLocationUpdates(): Boolean

    class LocationException(message: String): Exception()
}