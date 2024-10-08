package com.bk.bk1.utilities

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.bk.bk1.R
import com.bk.bk1.data.TrackDatabase
import com.bk.bk1.enums.SensorConnectionStatus
import com.bk.bk1.enums.TrackingStatus
import com.bk.bk1.events.ConnectionStatusChangedEvent
import com.bk.bk1.events.SensorAddressChangedEvent
import com.bk.bk1.events.TrackingStatusChangedEvent
import com.squareup.otto.Bus
import com.squareup.otto.Produce
import com.squareup.otto.Subscribe
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SensorService() : Service() {
    @Inject
    lateinit var db: TrackDatabase
    @Inject
    lateinit var sensorManager: SensorManager
    @Inject
    lateinit var trackingManager: TrackingManager
    @Inject
    lateinit var bus: Bus

    private var isRegisteredForBus = false
    private var sensorAddress = String()
    private var trackingStatus = TrackingStatus.NOT_TRACKING

    private var notification =
        NotificationCompat.Builder(this, "sensor_channel")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.bicycle_pin)


    // Called when service receives an action. Calls a method according to the received action.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.CONNECT.toString() -> {
                val sensorAddress = intent.getStringExtra("sensorAddress")
                sensorAddress?.let {
                    this.sensorAddress = it
                    bus.post(produceSensorAddressEvent())
                    startService()

                }
            }
            Actions.STOP_ALL.toString() -> stopService()
            Actions.START_TRACKING.toString() -> startTracking()
            Actions.STOP_TRACKING.toString() -> stopTracking()
        }
        return START_STICKY
    }

    // Registers service for event bus, creates a notification and starts service.
    private fun startService() {
        if (!isRegisteredForBus) {
            isRegisteredForBus = true
            bus.register(this)
        }
        sensorManager.connectToSensor(sensorAddress)
    }

    // Stops tracking, disconnects sensor, unregisters from event bus and stops self.
    private fun stopService() {
        stopTracking()
        sensorManager.disconnectSensor()
        sensorAddress = String()
        bus.post(produceSensorAddressEvent())
        bus.unregister(this)
        isRegisteredForBus = false
        stopSelf()
    }

    private fun startTracking() {
        trackingManager.startTracking()
        notification.setContentText(resources.getString(R.string.notif_text_tracking_on))
        startForeground(1, notification.build())
    }

    private fun stopTracking() {
        trackingManager.stopTracking()
        notification.setContentText(resources.getString(R.string.notif_text_tracking_off))
        startForeground(1, notification.build())
    }

    // Stops tracking if tracking is ON and sensor disconnects.
    @Subscribe
    fun onConnectionStatusChanged(event: ConnectionStatusChangedEvent) {
        if (event.connectionStatus <= SensorConnectionStatus.DISCONNECTED) {
            if (trackingStatus == TrackingStatus.TRACKING) {
                stopTracking()
            }
            notification.setContentTitle(resources.getString(R.string.notif_title_sensor_disconnected))
            startForeground(1, notification.build())
        }

        else if (event.connectionStatus == SensorConnectionStatus.CONNECTED) {
            notification.setContentTitle(resources.getString(R.string.notif_title_sensor_connected))
            notification.setContentText(resources.getString(R.string.notif_text_tracking_off))
            startForeground(1, notification.build())
        }
    }

    @Subscribe
    fun onTrackingStatusChanged(event: TrackingStatusChangedEvent) {
        trackingStatus = event.trackingStatus
    }

    @Produce
    fun produceSensorAddressEvent(): SensorAddressChangedEvent {
        return SensorAddressChangedEvent(sensorAddress)
    }


    // Defines actions for this service.
    enum class Actions {
        CONNECT, STOP_ALL, START_TRACKING, STOP_TRACKING
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): SensorService = this@SensorService
    }

    private val binder = LocalBinder()


}
