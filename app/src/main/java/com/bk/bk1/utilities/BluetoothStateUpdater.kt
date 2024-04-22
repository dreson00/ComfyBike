package com.bk.bk1.utilities

import android.bluetooth.BluetoothManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class BluetoothStateUpdater @Inject constructor(
    bluetoothManager: BluetoothManager
) {
    private val _bluetoothState = MutableStateFlow(true)
    val bluetoothState = _bluetoothState.asStateFlow()

    init {
        _bluetoothState.update { bluetoothManager.adapter.isEnabled }
    }
    fun updateBluetoothState(isEnabled: Boolean) {
        _bluetoothState.update { isEnabled }
    }
}