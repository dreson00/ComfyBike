package com.bk.bk1.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.bk.bk1.utilities.BluetoothScanManager
import com.bk.bk1.utilities.BluetoothStateUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SensorConnectScreenViewModel @Inject constructor(
    private val btService: BluetoothScanManager,
    private val bluetoothStateUpdater: BluetoothStateUpdater
) : ViewModel() {
    val devicesLiveData = btService.devicesLiveData
    val isBluetoothAdapterOn = bluetoothStateUpdater.bluetoothState.asLiveData()

    fun startScan() {
        btService.startScan()
    }
    fun stopScan() {
        btService.stopScan()
    }

}