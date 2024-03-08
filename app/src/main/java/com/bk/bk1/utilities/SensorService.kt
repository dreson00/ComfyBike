package com.bk.bk1.utilities

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.bk.bk1.R
import com.bk.bk1.data.SensorNotificationListener
import com.bk.bk1.data.TrackDatabase
import com.bk.bk1.models.BluetoothDeviceInfo
import com.bk.bk1.models.ComfortIndexRecord
import com.bk.bk1.models.LinearAcceleration
import com.bk.bk1.models.TrackRecord
import com.google.android.gms.location.LocationServices
import com.movesense.mds.Mds
import com.movesense.mds.MdsSubscription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt

class SensorService : Service() {

    companion object {
        val deviceInfo = BluetoothDeviceInfo(
            address = "",
            MutableLiveData(String()),
            MutableLiveData(0)
        )
        val isTrackingOn = MutableLiveData(false)
        var sensorData = MutableLiveData<LinearAcceleration>(null)
        val location = MutableLiveData<Location>(null)
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val channelId = "sensor_channel"
    private lateinit var locationClient: LocationClient
    private lateinit var mds: Mds
//    var deviceInfo = BluetoothDeviceInfo(
//        address = "",
//        MutableLiveData(String()),
//        MutableLiveData(0)
//    )

    private var sensorNotificationListener = SensorNotificationListener(::onNotification)
    private var mdsSubscription: MdsSubscription? = null

    private lateinit var db: TrackDatabase
    private var lastTimestamp = 0
    private var lastTrackId = 0
    private var oneSecondDataList = mutableListOf<LinearAcceleration>()

    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
        db = TrackDatabase.getDatabase(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.CONNECT.toString() -> {
                val sensorAddress = intent.getStringExtra("sensorAddress") as String?
                if (sensorAddress != null) {
                    startService()
                    connectToSensor(sensorAddress)
                }
            }
            Actions.STOP_ALL.toString() -> stopService()
            Actions.START_TRACKING.toString() -> startTracking()
            Actions.STOP_TRACKING.toString() -> stopTracking()
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
            .onEach { loc ->
                location.postValue(loc)
            }
            .launchIn(serviceScope)


        startForeground(1, notification.build())


    }

    private fun stopService() {
        disconnectSensor()
        stopSelf()
    }

    private fun startTracking() {
        deviceInfo.serialNumber.value?.let {
            subscribe(it)
        }
        isTrackingOn.postValue(true)
        serviceScope.launch {
            val trackRecord = TrackRecord(name = "Trasa", time = "time")
            lastTrackId =  db.trackRecordDao.upsertTrackRecord(trackRecord).toInt()
        }
    }

    private fun stopTracking() {
        isTrackingOn.postValue(false)
        mdsSubscription?.unsubscribe()
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

    private fun subscribe(serialNumber: String) {
        mdsSubscription = mds.subscribe(
            "suunto://MDS/EventListener",
            "{\"Uri\": \"$serialNumber/Meas/Acc/13\"}",
            sensorNotificationListener
        )
    }

    private fun onNotification(data: LinearAcceleration) {
        sensorData.postValue(data)
        if (data.Body.Timestamp - lastTimestamp < 1000) {
            oneSecondDataList.add(data)
        }
        else {
            lastTimestamp = data.Body.Timestamp
            if (oneSecondDataList.isNotEmpty()) {
                var accelerationPowSum = 0.0
                var filteredAccYCount = 0
                oneSecondDataList.forEach { item ->
                    val accY = item.Body.arrayAcc.first().y
                    val g = 9.8
                    val accThreshold = 1.0
                    if (accY / g >= accThreshold){
                        accelerationPowSum += (accY / g).pow(2.0)
                        filteredAccYCount++
                    }
                }
                if (filteredAccYCount > 0 && accelerationPowSum != 0.0) {
                    val comfortIndex = (1.0 / sqrt((1.0 / filteredAccYCount) * accelerationPowSum)).toFloat()
                    serviceScope.launch {
                        val currentLocation = location.value
                        currentLocation?.let {
                            db.comfortIndexRecordDao.upsertRecord(
                                ComfortIndexRecord(
                                    comfortIndex = comfortIndex,
                                    trackRecordId = lastTrackId,
                                    latitude = currentLocation.latitude,
                                    longitude = currentLocation.longitude
                                )
                            )
                        }
                    }
                }
                oneSecondDataList = mutableListOf()
            }

        }
    }

    override fun onDestroy() {
        stopTracking()
        disconnectSensor()
        super.onDestroy()
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
