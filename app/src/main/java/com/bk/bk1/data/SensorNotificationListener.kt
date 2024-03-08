package com.bk.bk1.data

import android.util.Log
import com.bk.bk1.models.LinearAcceleration
import com.movesense.mds.MdsException
import com.movesense.mds.MdsNotificationListener
import kotlinx.serialization.json.Json

class SensorNotificationListener(private val notificationCallback: (LinearAcceleration) -> Unit): MdsNotificationListener {

    override fun onNotification(data: String?) {
        if(data != null) {
            notificationCallback(Json.decodeFromString<LinearAcceleration>(data))
        }
    }

    override fun onError(p0: MdsException?) {
        p0?.let {
            Log.e(null, p0.message!!)
        }

    }


}