package com.bk.bk1.viewModels

import androidx.lifecycle.ViewModel
import com.bk.bk1.utilities.BluetoothScanService

class SensorConnectScreenViewModel(private val btService: BluetoothScanService) : ViewModel() {
    val devicesLiveData = btService.devicesLiveData

    fun startScan() {
        btService.startScan()
    }
    fun stopScan() {
        btService.stopScan()
    }

}