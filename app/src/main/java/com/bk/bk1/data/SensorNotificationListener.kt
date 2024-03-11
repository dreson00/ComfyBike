package com.bk.bk1.data

import android.util.Log
import com.bk.bk1.events.SensorDataReceivedEvent
import com.bk.bk1.models.LinearAcceleration
import com.bk.bk1.utilities.BusProvider
import com.movesense.mds.MdsException
import com.movesense.mds.MdsNotificationListener
import com.squareup.otto.Produce
import kotlinx.serialization.json.Json

class SensorNotificationListener: MdsNotificationListener {
    private val bus = BusProvider.getEventBus()
    private var data: LinearAcceleration? = null

    override fun onNotification(data: String?) {
        if(data != null) {
            this.data = Json.decodeFromString<LinearAcceleration>(data)
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