package com.bk.bk1.utilities

import android.location.Location
import com.bk.bk1.data.ComfortIndexRecordDao
import com.bk.bk1.data.TrackRecordDao
import com.bk.bk1.enums.TrackingStatus
import com.bk.bk1.events.CurrentTrackIdChangedEvent
import com.bk.bk1.events.SensorDataReceivedEvent
import com.bk.bk1.events.TrackingStatusChangedEvent
import com.bk.bk1.models.ComfortIndexRecord
import com.bk.bk1.models.Imu
import com.bk.bk1.models.TrackRecord
import com.squareup.otto.Produce
import com.squareup.otto.Subscribe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt

class TrackingManager @Inject constructor(
    private val comfortIndexRecordDao: ComfortIndexRecordDao,
    private val trackRecordDao: TrackRecordDao,
    private val locationClient: LocationClient
) {
    private var trackingStatus = TrackingStatus.NOT_TRACKING
    private var lastTrackId: Int? = null
    private var lastTimestamp = 0
    private var oneSecondDataList = mutableListOf<Imu>()
    private var location: Location? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val bus = BusProvider.getEventBus()

    var sensorOrientationCalculator = SensorOrientationCalculator()

    fun startTracking() {
        bus.register(this)
        locationClient
            .getLocationUpdates(500L)
            .catch { e -> e.printStackTrace() }
            .onEach { loc ->
                location = loc
            }
            .launchIn(scope)

        runBlocking {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateString = formatter.format(Date())
            val trackRecord = TrackRecord(name = "Trasa", time = dateString)
            lastTrackId = trackRecordDao.upsertTrackRecord(trackRecord).toInt()
        }

        sensorOrientationCalculator = SensorOrientationCalculator()
//        bus.register(sensorOrientationCalculator)

        bus.post(produceCurrentTrackIdChangedEvent())
        trackingStatus = TrackingStatus.TRACKING
        bus.post(produceTrackingStatusChangedEvent())
    }

    fun stopTracking() {
        if (trackingStatus <= TrackingStatus.NOT_TRACKING) {
            return
        }
        val toDeleteCandidateId = lastTrackId
        lastTrackId = null
        bus.post(produceCurrentTrackIdChangedEvent())
        trackingStatus = TrackingStatus.NOT_TRACKING
        bus.post(produceTrackingStatusChangedEvent())

        scope.launch {
            toDeleteCandidateId?.let {
                if (comfortIndexRecordDao.getComfortIndexRecordCount(it) == 0) {
                    trackRecordDao.deleteTrackRecord(it)
                }
            }
        }

        locationClient.removeLocationUpdates()
//        bus.unregister(sensorOrientationCalculator)
        bus.unregister(this)
    }

    @Subscribe
    fun onSensorDataReceived(event: SensorDataReceivedEvent) {
        val sensorData = event.data ?: return
        sensorOrientationCalculator.onSensorChanged(event.data)
        if (sensorData.Body.Timestamp - lastTimestamp < 1000) {
            oneSecondDataList.add(sensorData)
        }
        else {
            lastTimestamp = sensorData.Body.Timestamp
            if (oneSecondDataList.isNotEmpty()) {
                saveSensorData()
                oneSecondDataList = mutableListOf()
            }
        }
    }

    private fun saveSensorData() {
        var accelerationPowSum = 0.0
        var filteredAccYCount = 0
        val g = 9.8
        val accThreshold = 1.0
        oneSecondDataList.forEach { item ->
            item.Body.arrayAcc.forEach { accelerationData ->
                val accY = accelerationData.y / g
                if (accY >= accThreshold) {
                    accelerationPowSum += (accY).pow(2.0)
                    filteredAccYCount++
                }
            }
        }
        if (filteredAccYCount > 0 && accelerationPowSum != 0.0) {
            val comfortIndex = (1.0 / sqrt((1.0 / filteredAccYCount) * accelerationPowSum)).toFloat()
            scope.launch {
                val currentLocation = location
                if (currentLocation != null && lastTrackId != null && currentLocation.hasSpeed()) {
                    comfortIndexRecordDao.upsertRecord(
                        ComfortIndexRecord(
                            comfortIndex = comfortIndex,
                            bicycleSpeed = currentLocation.speed,
                            trackRecordId = lastTrackId!!,
                            latitude = currentLocation.latitude,
                            longitude = currentLocation.longitude
                        )
                    )
                }
            }
        }
    }

    @Produce
    fun produceTrackingStatusChangedEvent(): TrackingStatusChangedEvent {
        return TrackingStatusChangedEvent(trackingStatus)
    }

    @Produce
    fun produceCurrentTrackIdChangedEvent(): CurrentTrackIdChangedEvent {
        return CurrentTrackIdChangedEvent(lastTrackId)
    }
}

