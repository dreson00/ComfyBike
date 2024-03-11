package com.bk.bk1.utilities

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationClient {
    fun getLocationUpdates(interval: Long): Flow<Location>

    fun removeLocationUpdates()

    class LocationExcepetion(message: String): Exception()
}