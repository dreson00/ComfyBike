package com.bk.bk1.data

import android.util.Log
import com.bk.bk1.events.SensorDataReceivedEvent
import com.bk.bk1.models.Imu
import com.movesense.mds.MdsException
import com.movesense.mds.MdsNotificationListener
import com.squareup.otto.Bus
import com.squareup.otto.Produce
import kotlinx.serialization.json.Json

class SensorNotificationListener(
    private val bus: Bus
): MdsNotificationListener {
    private var data: Imu? = null

    override fun onNotification(data: String?) {
        if(data != null) {
            this.data = Json.decodeFromString<Imu>(data)
            bus.post(produceSensorDataReceivedEvent())
        }
    }

    override fun onError(p0: MdsException?) {
        p0?.let {
            Log.e(null, p0.message!!)
        }
    }

    @Produce
    fun produceSensorDataReceivedEvent(): SensorDataReceivedEvent {
        return SensorDataReceivedEvent(data)
    }
}