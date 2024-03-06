package com.bk.bk1.utilities

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationClient {
    fun getLocationUpdates(interval: Long): Flow<Location>

    class LocationExcpetion(message: String): Exception()
}