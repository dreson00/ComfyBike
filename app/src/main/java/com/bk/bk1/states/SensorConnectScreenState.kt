package com.bk.bk1.states

import android.bluetooth.BluetoothDevice

data class SensorConnectScreenState(
    val deviceList: List<BluetoothDevice> = emptyList(),
    val isBluetoothAdapterOn: Boolean = true,
    val showBackgroundServiceRequest: Boolean = false,
    val showPostNotificationRequest: Boolean = false,
    val blockButtonClicks: Boolean = false,
)