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
        deviceAddress?.let {
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
            "{\"Uri\": \"${deviceSerialNumber}/Meas/IMU9/26\"}",
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

