package com.bk.bk1.utilities

import com.bk.bk1.enums.TrackingStatus
import com.bk.bk1.events.SensorAddressChangedEvent
import com.bk.bk1.events.SerialNumberChangedEvent
import com.bk.bk1.events.TrackingStatusChangedEvent
import com.movesense.mds.Mds
import com.movesense.mds.MdsSubscription
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import javax.inject.Inject

class SensorManager @Inject constructor(
    private val bus: Bus,
    private var mds: Mds,
) {
    private var mdsSubscription: MdsSubscription? = null
    private var sensorConnectionListener = SensorConnectionListener(bus)
    private var sensorNotificationListener = SensorNotificationListener(bus)
    private var deviceAddress: String? = null
    private var deviceSerialNumber = String()

    private var isSubscribed = false
    private var isRegisteredForBus = false


    // Registers self and sensorConnectionListener for bus and connects to sensor.
    fun connectToSensor(sensorAddress: String) {
        if (!isRegisteredForBus) {
            bus.register(this)
            bus.register(sensorConnectionListener)
            isRegisteredForBus = true
        }

        mds.connect(sensorAddress, sensorConnectionListener)
    }

    // Stops subscribing for sensor data, disconnects from sensor and unregisters from event bus.
    fun disconnectSensor() {
        unsubscribe()
        deviceAddress?.let {
            mds.disconnect(it)
        }
        bus.unregister(this)
        isRegisteredForBus = false
        bus.unregister(sensorConnectionListener)
    }

    // Subscribes for sensor data.
    private fun subscribe() {
        bus.register(sensorNotificationListener)
        mdsSubscription = mds?.subscribe(
            "suunto://MDS/EventListener",
            "{\"Uri\": \"${deviceSerialNumber}/Meas/Acc/26\"}",
            sensorNotificationListener
        )
        isSubscribed = true
    }

    // Unsubscribes from sensor.
    private fun unsubscribe() {
        if (isSubscribed) {
            mdsSubscription?.unsubscribe()
            bus.unregister(sensorNotificationListener)
            isSubscribed = false
        }
    }

    @Subscribe
    fun onSensorAddressChanged(event: SensorAddressChangedEvent) {
        deviceAddress = event.sensorAddress
    }

    @Subscribe
    fun onSerialNumberChanged(event: SerialNumberChangedEvent) {
        event.serialNumber?.let {
            deviceSerialNumber = event.serialNumber
        }
    }

    @Subscribe
    fun onTrackingStatusChanged(event: TrackingStatusChangedEvent) {
        if (event.trackingStatus == TrackingStatus.TRACKING) {
            subscribe()
        }
        else {
            unsubscribe()
        }
    }

}

