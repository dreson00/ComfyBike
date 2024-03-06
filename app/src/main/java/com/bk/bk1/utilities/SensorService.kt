package com.bk.bk1.utilities

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.bk.bk1.R
import com.bk.bk1.models.BluetoothDeviceInfo
import com.google.android.gms.location.LocationServices
import com.movesense.mds.Mds
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach

class SensorService : Service() {

    companion object {
        val deviceInfo = BluetoothDeviceInfo(
            address = "",
            MutableLiveData(String()),
            MutableLiveData(0)
        )
    }

//    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val channelId = "sensor_channel"
    private lateinit var locationClient: LocationClient
    private lateinit var mds: Mds
//    var deviceInfo = BluetoothDeviceInfo(
//        address = "",
//        MutableLiveData(String()),
//        MutableLiveData(0)
//    )




    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.toString() -> {
                val sensorAddress = intent.getStringExtra("sensorAddress") as String?
                if (sensorAddress != null) {
                    startService()
                    connectToSensor(sensorAddress)
                }
            }
            Actions.STOP.toString() -> stopService()
        }
        return START_STICKY
    }

    private fun startService() {
        val notification =
            NotificationCompat.Builder(this, channelId)
                .setContentTitle("Sensor Service")
                .setContentText("Running...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)

        locationClient
            .getLocationUpdates(1000L)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->

            }
        startForeground(1, notification.build())
    }

    private fun stopService() {
        disconnectSensor()
        stopSelf()
    }

    private fun connectToSensor(sensorAddress: String) {
        mds = Mds.builder().build(applicationContext)
        mds.connect(sensorAddress, SensorConnectionListener(deviceInfo))
        deviceInfo.address = sensorAddress
    }

    private fun disconnectSensor() {
        deviceInfo.address?.let {
            mds.disconnect(it)
            deviceInfo.address = ""
        }
    }

//    override fun onBind(intent: Intent): IBinder? {
//        return null
//    }

    override fun onDestroy() {
        disconnectSensor()
        super.onDestroy()
    }

    enum class Actions {
        START, STOP
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): SensorService = this@SensorService
    }

    private val binder = LocalBinder()


}
