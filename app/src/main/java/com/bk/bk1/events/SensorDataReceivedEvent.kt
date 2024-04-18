package com.bk.bk1.events

import com.bk.bk1.models.Imu

data class SensorDataReceivedEvent(
    val data: Imu?
)