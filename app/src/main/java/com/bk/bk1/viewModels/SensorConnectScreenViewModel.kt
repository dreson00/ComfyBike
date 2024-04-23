package com.bk.bk1.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.bk.bk1.states.SensorConnectScreenState
import com.bk.bk1.utilities.BluetoothScanManager
import com.bk.bk1.utilities.BluetoothStateUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SensorConnectScreenViewModel @Inject constructor(
    private val btService: BluetoothScanManager,
    private val bluetoothStateUpdater: BluetoothStateUpdater
) : ViewModel() {
    private val _deviceListFlow = btService.devicesLiveData.asFlow()
    private val _isBluetoothAdapterOn = bluetoothStateUpdater.bluetoothState
    private val _state =  MutableStateFlow(SensorConnectScreenState())

    val state = combine(
        _state,
        _deviceListFlow,
        _isBluetoothAdapterOn
    ) {
        state,
        deviceList,
        isBluetoothAdapterOn ->

        state.copy(
            deviceList = deviceList,
            isBluetoothAdapterOn = isBluetoothAdapterOn
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), SensorConnectScreenState())

    fun setShowBackgroundServiceRequest(value: Boolean) {
        _state.update { it.copy(showBackgroundServiceRequest = value) }
    }

    fun setShowPostNotificationRequest(value: Boolean) {
        _state.update { it.copy(showPostNotificationRequest = value) }
    }

    fun setBlockButtonClicks(value: Boolean) {
        _state.update { it.copy(blockButtonClicks = value) }
    }


    fun startScan() {
        btService.startScan()
    }
    fun stopScan() {
        btService.stopScan()
    }

}