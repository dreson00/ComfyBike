package com.bk.bk1.events

import com.bk.bk1.models.LinearAcceleration

data class SensorDataReceivedEvent(
    val data: LinearAcceleration?
)