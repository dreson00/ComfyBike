package com.bk.bk1.utilities

import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class LocationStateUpdater @Inject constructor() {
    val locationState = MutableStateFlow(false)

    fun updateLocationState(isEnabled: Boolean) {
        locationState.value = isEnabled
    }
}