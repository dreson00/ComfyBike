package com.bk.bk1.viewModels

import androidx.lifecycle.ViewModel
import com.bk.bk1.utilities.BluetoothScanManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SensorConnectScreenViewModel @Inject constructor(private val btService: BluetoothScanManager) : ViewModel() {
    val devicesLiveData = btService.devicesLiveData

    fun startScan() {
        btService.startScan()
    }
    fun stopScan() {
        btService.stopScan()
    }

}