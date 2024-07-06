package com.bk.bk1.utilities

import android.location.Location
import com.bk.bk1.data.ComfortIndexRecordRepository
import com.bk.bk1.data.TrackRecordRepository
import com.bk.bk1.enums.TrackingStatus
import com.bk.bk1.events.CurrentTrackIdChangedEvent
import com.bk.bk1.events.SensorDataReceivedEvent
import com.bk.bk1.events.TrackingStatusChangedEvent
import com.bk.bk1.models.ComfortIndexRecord
import com.bk.bk1.models.LinearAcceleration
import com.bk.bk1.models.TrackRecord
import com.squareup.otto.Bus
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
    private val comfortIndexRecordRepository: ComfortIndexRecordRepository,
    private val trackRecordRepository: TrackRecordRepository,
    private val bus: Bus,
    private val locationClient: LocationClient
) {
    private var trackingStatus = TrackingStatus.NOT_TRACKING
    private var lastTrackId: Int? = null
    private var lastTimestamp = 0
    private var oneSecondDataList = mutableListOf<LinearAcceleration>()
    private var location: Location? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun startTracking() {
        // Registers for event bus and GPS location changes.
        bus.register(this)
        locationClient
            .getLocationUpdates(500L)
            .catch { e -> e.printStackTrace() }
            .onEach { loc ->
                location = loc
            }
            .launchIn(scope)

        // Creates a new Track record in the DB
        runBlocking {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateString = formatter.format(Date())
            val trackRecord = TrackRecord(name = "Trasa", time = dateString)
            lastTrackId = trackRecordRepository.upsertTrackRecord(trackRecord).toInt()
        }

        // Posts events about new TrackId and tracking status.
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

        // Posts events about new TrackId and tracking status.
        bus.post(produceCurrentTrackIdChangedEvent())
        trackingStatus = TrackingStatus.NOT_TRACKING
        bus.post(produceTrackingStatusChangedEvent())

        // Deletes last TrackRecord if there are no ComfortIndexRecords associated with the TrackRecord.
        scope.launch {
            toDeleteCandidateId?.let {
                if (comfortIndexRecordRepository.getComfortIndexRecordCount(it) == 0) {
                    trackRecordRepository.deleteTrackRecord(it)
                }
            }
        }

        // Unregisters from event bus and GPS location changes.
        locationClient.removeLocationUpdates()
//        bus.unregister(sensorOrientationCalculator)
        bus.unregister(this)
    }

    @Subscribe
    fun onSensorDataReceived(event: SensorDataReceivedEvent) {
        val sensorData = event.data ?: return

        // Adds received sensor data to oneSecondDataList
        // if the time between current data and the last processed data is less than one second.
        if (sensorData.Body.Timestamp - lastTimestamp < 1000) {
            oneSecondDataList.add(sensorData)
        }
        // Updates timestamp, calls processAndSaveSensorData and clears oneSecondDataList
        // if one second or more elapsed between current and last processed data.
        else {
            lastTimestamp = sensorData.Body.Timestamp
            if (oneSecondDataList.isNotEmpty()) {
                processAndSaveSensorData()
                oneSecondDataList = mutableListOf()
                oneSecondDataList.add(sensorData)
            }
        }
    }

    private fun processAndSaveSensorData() {
        // Calculates Dynamic Comfort Index according to a formula.
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
                // Gets current location and inserts a new ComfortIndexRecord into DB.
                val currentLocation = location
                if (currentLocation != null && lastTrackId != null && currentLocation.hasSpeed()) {
                    comfortIndexRecordRepository.upsertRecord(
                        ComfortIndexRecord(
                            comfortIndex = comfortIndex,
                            bicycleSpeed = currentLocation.speed * 3.6f,
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

