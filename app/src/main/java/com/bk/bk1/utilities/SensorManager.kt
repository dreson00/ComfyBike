package com.bk.bk1.utilities

import com.bk.bk1.data.SensorNotificationListener
import com.bk.bk1.events.ConnectionStatusChangedEvent
import com.bk.bk1.events.SensorAddressChangedEvent
import com.bk.bk1.events.SerialNumberChangedEvent
import com.bk.bk1.models.BluetoothDeviceInfo
import com.movesense.mds.Mds
import com.movesense.mds.MdsSubscription
import com.squareup.otto.Subscribe

class SensorManager(
    private var mds: Mds,
) {

    private var mdsSubscription: MdsSubscription? = null
    private var sensorNotificationListener = SensorNotificationListener()
    private val deviceInfo: BluetoothDeviceInfo = BluetoothDeviceInfo(null, String(), 0)
    private val bus = BusProvider.getEventBus()
    private val sensorConnectionListener = SensorConnectionListener()
    private var isSubscribed = false

    fun connectToSensor(sensorAddress: String) {
        bus.register(this)
        bus.register(sensorConnectionListener)
        mds.connect(sensorAddress, sensorConnectionListener)
    }

    fun disconnectSensor() {
        unsubscribe()
        deviceInfo.address?.let {
            mds.disconnect(it)
        }
        bus.unregister(this)
        bus.unregister(sensorConnectionListener)
    }

    fun subscribe() {
        bus.register(sensorNotificationListener)
        mdsSubscription = mds.subscribe(
            "suunto://MDS/EventListener",
            "{\"Uri\": \"${deviceInfo.serialNumber}/Meas/Acc/26\"}",
            sensorNotificationListener
        )
        isSubscribed = true
    }

    fun unsubscribe() {
        if (isSubscribed) {
            mdsSubscription?.unsubscribe()
            bus.unregister(sensorNotificationListener)
            isSubscribed = false
        }
    }


    @Subscribe
    fun onSensorAddressChanged(event: SensorAddressChangedEvent) {
        deviceInfo.address = event.sensorAddress
    }

    @Subscribe
    fun onSerialNumberChanged(event: SerialNumberChangedEvent) {
        event.serialNumber?.let {
            deviceInfo.serialNumber = event.serialNumber
        }
    }

    @Subscribe
    fun onConnectionStatusChanged(event: ConnectionStatusChangedEvent) {
        deviceInfo.connectionStatus = event.connectionStatus
    }

}

