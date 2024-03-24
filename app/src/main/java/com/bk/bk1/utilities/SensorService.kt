package com.bk.bk1.utilities

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.bk.bk1.R
import com.bk.bk1.data.TrackDatabase
import com.bk.bk1.events.ConnectionStatusChangedEvent
import com.bk.bk1.events.SensorAddressChangedEvent
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

    private val bus = BusProvider.getEventBus()
    private var isRegisteredForBus = false
    private var sensorAddress = String()
    private val handler = Handler(Looper.getMainLooper())


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.CONNECT.toString() -> {
                val sensorAddress = intent.getStringExtra("sensorAddress") as String?
                sensorAddress?.let {
                    this.sensorAddress = it
                    bus.post(produceSensorAddressEvent())
                    startService()
                    sensorManager.connectToSensor(it)
                }
            }
            Actions.STOP_ALL.toString() -> stopService()
            Actions.START_TRACKING.toString() -> startTracking()
            Actions.STOP_TRACKING.toString() -> stopTracking()
        }
        return START_STICKY
    }

    private fun startService() {
        if (!isRegisteredForBus) {
            isRegisteredForBus = true
            bus.register(this)
        }
        val notification =
            NotificationCompat.Builder(this, "sensor_channel")
                .setContentTitle("Sensor Service")
                .setContentText("Running...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)

        startForeground(1, notification.build())
    }

    private fun stopService() {
        stopTracking()
        handler.removeCallbacksAndMessages(null)
        sensorManager.disconnectSensor()
        sensorAddress = String()
        bus.post(produceSensorAddressEvent())
        bus.unregister(this)
        isRegisteredForBus = false
        stopSelf()
    }

    private fun pauseForDisconnectService() {
        stopTracking()
        handler.removeCallbacksAndMessages(null)
        sensorAddress = String()

    }

    private fun startTracking() {
        trackingManager.startTracking()
        sensorManager.subscribe()
    }

    private fun stopTracking() {
        trackingManager.stopTracking()
        sensorManager.unsubscribe()
    }

    @Subscribe
    fun onConnectionStatusChanged(event: ConnectionStatusChangedEvent) {
        if (event.connectionStatus <= 0) {
            handler.postDelayed(::stopTracking, 60000)
        }
        if (event.connectionStatus > 0) {
            handler.removeCallbacksAndMessages(null)
        }
    }

    @Produce
    fun produceSensorAddressEvent(): SensorAddressChangedEvent {
        return SensorAddressChangedEvent(sensorAddress)
    }


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
