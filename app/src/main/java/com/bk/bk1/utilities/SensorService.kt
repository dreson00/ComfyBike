package com.bk.bk1.utilities

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.bk.bk1.R
import com.bk.bk1.data.TrackDatabase
import com.bk.bk1.events.BluetoothAdapterStatusChangedEvent
import com.bk.bk1.events.SensorAddressChangedEvent
import com.google.android.gms.location.LocationServices
import com.movesense.mds.Mds
import com.squareup.otto.Produce
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class SensorService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val channelId = "sensor_channel"

    private lateinit var mds: Mds
    private lateinit var sensorManager: SensorManager
    private lateinit var trackingManager: TrackingManager

    private val bus = BusProvider.getEventBus()
    private var sensorAddress = String()

    private var bluetoothAdapterStatus = 0

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    BluetoothAdapter.STATE_OFF -> {
                        bluetoothAdapterStatus = BluetoothAdapter.STATE_OFF
                        bus.post(produceBluetoothAdapterStatusChangedEvent())
                    }
                    BluetoothAdapter.STATE_ON -> {
                        bluetoothAdapterStatus = BluetoothAdapter.STATE_ON
                        bus.post(produceBluetoothAdapterStatusChangedEvent())
                    }
                }
            }
        }
    }


    override fun onCreate() {
        super.onCreate()
        mds = Mds.builder().build(applicationContext)
        sensorManager = SensorManager(mds)
        val locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
        val db = TrackDatabase.getDatabase(applicationContext)
        trackingManager = TrackingManager(
            serviceScope,
            db.comfortIndexRecordDao,
            db.trackRecordDao,
            locationClient)
    }

//    private fun checkAndSetBluetoothAdapterStatus() {
//        bluetoothAdapter?.let {
//            if (bluetoothAdapter.isEnabled || bluetoothAdapter.i) {
//                bluetoothAdapterStatus = BluetoothAdapter.STATE_ON
//            }
//        }
//    }

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
        bus.register(this)
        val notification =
            NotificationCompat.Builder(this, channelId)
                .setContentTitle("Sensor Service")
                .setContentText("Running...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothStateReceiver, filter)

        startForeground(1, notification.build())
    }

    private fun stopService() {
        unregisterReceiver(bluetoothStateReceiver)
        stopTracking()
        sensorManager.disconnectSensor()
        sensorAddress = String()
        bus.post(produceSensorAddressEvent())
        bus.unregister(this)
        stopSelf()
    }

    private fun startTracking() {
        trackingManager.startTracking()
        sensorManager.subscribe()
    }

    private fun stopTracking() {
        trackingManager.stopTracking()
        sensorManager.unsubscribe()
    }

    @Produce
    fun produceSensorAddressEvent(): SensorAddressChangedEvent {
        return SensorAddressChangedEvent(sensorAddress)
    }

    @Produce
    fun produceBluetoothAdapterStatusChangedEvent(): BluetoothAdapterStatusChangedEvent {
        return BluetoothAdapterStatusChangedEvent(bluetoothAdapterStatus)
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
