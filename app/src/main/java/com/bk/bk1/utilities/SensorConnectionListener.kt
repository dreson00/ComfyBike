package com.bk.bk1.utilities

import com.bk.bk1.models.BluetoothDeviceInfo
import com.movesense.mds.MdsConnectionListener
import com.movesense.mds.MdsException

class SensorConnectionListener(val deviceInfo: BluetoothDeviceInfo) : MdsConnectionListener {
    override fun onConnect(p0: String?) {
        deviceInfo.connectionState.postValue(1)
    }

    override fun onConnectionComplete(p0: String?, serial: String?) {
        deviceInfo.connectionState.postValue(2)
        deviceInfo.serialNumber.postValue(serial)
    }

    override fun onError(p0: MdsException?) {
        deviceInfo.connectionState.postValue(-1)
    }

    override fun onDisconnect(p0: String?) {
        deviceInfo.connectionState.postValue(0)
        deviceInfo.serialNumber.postValue(String())
    }
}