package com.bk.bk1.models

import androidx.lifecycle.MutableLiveData

class BluetoothDeviceInfo(
    val name: String?,
    val address: String?,
    var serialNumber: MutableLiveData<String>,
    var connectionState: MutableLiveData<Int>
)