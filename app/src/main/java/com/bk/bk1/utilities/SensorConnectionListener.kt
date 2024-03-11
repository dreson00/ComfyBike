package com.bk.bk1.utilities

import com.bk.bk1.events.ConnectionStatusChangedEvent
import com.bk.bk1.events.SerialNumberChangedEvent
import com.movesense.mds.MdsConnectionListener
import com.movesense.mds.MdsException
import com.squareup.otto.Produce

class SensorConnectionListener() : MdsConnectionListener {
    private val bus = BusProvider.getEventBus()
    private var serialNumber: String? = null
    private var connectionStatus = 0

    override fun onConnect(p0: String?) {
        connectionStatus = 1
        bus.post(produceConnectionStatusChangedEvent())
    }

    override fun onConnectionComplete(p0: String?, serial: String?) {
        connectionStatus = 2
        bus.post(produceConnectionStatusChangedEvent())
        serialNumber = serial
        bus.post(produceSerialNumberChangedEvent())
    }

    override fun onError(p0: MdsException?) {
        connectionStatus = -1
        bus.post(produceConnectionStatusChangedEvent())
    }

    override fun onDisconnect(p0: String?) {
        connectionStatus = 0
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

