package com.bk.bk1.utilities

import com.bk.bk1.enums.SensorConnectionStatus
import com.bk.bk1.events.ConnectionStatusChangedEvent
import com.bk.bk1.events.SerialNumberChangedEvent
import com.movesense.mds.MdsConnectionListener
import com.movesense.mds.MdsException
import com.squareup.otto.Bus
import com.squareup.otto.Produce

class SensorConnectionListener(
    private val bus: Bus
) : MdsConnectionListener {
    private var serialNumber: String? = null
    private var connectionStatus = SensorConnectionStatus.DISCONNECTED

    override fun onConnect(p0: String?) {
        connectionStatus = SensorConnectionStatus.CONNECTING
        bus.post(produceConnectionStatusChangedEvent())
    }

    override fun onConnectionComplete(p0: String?, serial: String?) {
        connectionStatus = SensorConnectionStatus.CONNECTED
        bus.post(produceConnectionStatusChangedEvent())
        serialNumber = serial
        bus.post(produceSerialNumberChangedEvent())
    }

    override fun onError(p0: MdsException?) {
        connectionStatus = SensorConnectionStatus.ERROR
        bus.post(produceConnectionStatusChangedEvent())
    }

    override fun onDisconnect(p0: String?) {
        connectionStatus = SensorConnectionStatus.DISCONNECTED
        bus.post(produceConnectionStatusChangedEvent())
        serialNumber = String()
        bus.post(produceSerialNumberChangedEvent())
    }

    @Produce
    fun produceConnectionStatusChangedEvent(): ConnectionStatusChangedEvent {
        return ConnectionStatusChangedEvent(connectionStatus)
    }

    @Produce
    fun produceSerialNumberChangedEvent(): SerialNumberChangedEvent {
        return SerialNumberChangedEvent(serialNumber)
    }
}

