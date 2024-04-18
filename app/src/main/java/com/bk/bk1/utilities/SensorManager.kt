package com.bk.bk1.utilities

import android.bluetooth.BluetoothManager
import com.bk.bk1.data.SensorNotificationListener
import com.bk.bk1.events.ConnectionStatusChangedEvent
import com.bk.bk1.events.SensorAddressChangedEvent
import com.bk.bk1.events.SerialNumberChangedEvent
import com.bk.bk1.events.TrackingStatusChangedEvent
import com.bk.bk1.models.BluetoothDeviceInfo
import com.movesense.mds.Mds
import com.movesense.mds.MdsSubscription
import com.squareup.otto.Subscribe
import javax.inject.Inject

class SensorManager @Inject constructor(
    private var mds: Mds,
    private var bluetoothManager: BluetoothManager
) {
    private var mdsSubscription: MdsSubscription? = null
    private var sensorConnectionListener = SensorConnectionListener()
    private var sensorNotificationListener = SensorNotificationListener()
    private val deviceInfo: BluetoothDeviceInfo = BluetoothDeviceInfo(null, String(), 0)
    private val bus = BusProvider.getEventBus()
    private var isSubscribed = false
    private var isRegisteredForBus = false


    fun connectToSensor(sensorAddress: String) {
        if (!isRegisteredForBus) {
            bus.register(this)
            bus.register(sensorConnectionListener)
            isRegisteredForBus = true
        }

        mds.connect(sensorAddress, sensorConnectionListener)
    }

    fun disconnectSensor() {
        unsubscribe()
        deviceInfo.address?.let {
            mds.disconnect(it)
        }
        bus.unregister(this)
        bus.unregister(sensorConnectionListener)
        isRegisteredForBus = false
    }

    private fun subscribe() {
        bus.register(sensorNotificationListener)
        mdsSubscription = mds?.subscribe(
            "suunto://MDS/EventListener",
            "{\"Uri\": \"${deviceInfo.serialNumber}/Meas/IMU9/26\"}",
            sensorNotificationListener
        )
        isSubscribed = true
    }

    private fun unsubscribe() {
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

    @Subscribe
    fun onTrackingStatusChanged(event: TrackingStatusChangedEvent) {
        if (event.trackingStatus == 1) {
            subscribe()
        }
        else {
            unsubscribe()
        }
    }

}

