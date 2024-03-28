package com.bk.bk1.utilities

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class LocationStateUpdater @Inject constructor() {
    private val _locationState = MutableStateFlow(true)
    val locationState: Flow<Boolean> = _locationState

    fun updateLocationState(isEnabled: Boolean) {
        _locationState.value = isEnabled
    }
}