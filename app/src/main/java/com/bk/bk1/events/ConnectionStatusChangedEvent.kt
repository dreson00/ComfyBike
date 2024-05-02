package com.bk.bk1.events

import com.bk.bk1.enums.SensorConnectionStatus

data class ConnectionStatusChangedEvent(
    val connectionStatus: SensorConnectionStatus
)