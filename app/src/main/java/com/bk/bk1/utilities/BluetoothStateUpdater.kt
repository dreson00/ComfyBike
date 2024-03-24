package com.bk.bk1.utilities

import android.bluetooth.BluetoothManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class BluetoothStateUpdater @Inject constructor(
    bluetoothManager: BluetoothManager
) {
    private val _bluetoothState = MutableStateFlow(true)
    val bluetoothState: Flow<Boolean> = _bluetoothState

    init {
        _bluetoothState.value = bluetoothManager.adapter.isEnabled
    }
    fun updateBluetoothState(isEnabled: Boolean) {
        _bluetoothState.value = isEnabled
    }
}