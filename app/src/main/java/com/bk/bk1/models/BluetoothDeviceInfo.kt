package com.bk.bk1.models

import com.bk.bk1.enums.SensorConnectionStatus

class BluetoothDeviceInfo(
    var address: String?,
    var serialNumber: String,
    var connectionStatus: SensorConnectionStatus
)