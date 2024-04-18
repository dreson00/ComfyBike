package com.bk.bk1.viewModels


import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import android.os.CountDownTimer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bk.bk1.data.ComfortIndexRecordDao
import com.bk.bk1.data.TrackRecordDao
import com.bk.bk1.events.ConnectionStatusChangedEvent
import com.bk.bk1.events.CurrentTrackIdChangedEvent
import com.bk.bk1.events.TrackingStatusChangedEvent
import com.bk.bk1.models.ComfortIndexRecord
import com.bk.bk1.models.ExperimentalData
import com.bk.bk1.utilities.BluetoothStateUpdater
import com.bk.bk1.utilities.BusProvider
import com.bk.bk1.utilities.LocationClient
import com.bk.bk1.utilities.LocationStateUpdater
import com.squareup.otto.Subscribe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("MissingPermission")
@HiltViewModel
class MainMapScreenViewModel @Inject constructor(
    private val application: Application,
    private val locationClient: LocationClient,
    private val trackRecordDao: TrackRecordDao,
    private val comfortIndexRecordDao: ComfortIndexRecordDao,
    private val bluetoothStateUpdater: BluetoothStateUpdater,
    private val locationStateUpdater: LocationStateUpdater
) : ViewModel() {

    var cameraFollow by mutableStateOf(true)
    var location = MutableLiveData<Location>(null)
    val showLocationPermissionRequest = MutableLiveData<String>(null)

    val currentTrackId = MutableLiveData<Int?>(null)
    var currentComfortIndexRecordsFlow = emptyFlow<List<ComfortIndexRecord>>()
    var firstComfortIndexRecordForAllExceptCurrent = emptyFlow<List<ComfortIndexRecord>>()
    val connectionStatus = MutableLiveData(0)
    val isBluetoothAdapterOn = bluetoothStateUpdater.bluetoothState.asLiveData()
    val isLocationEnabled = locationStateUpdater.locationState.asLiveData()
    private val bus = BusProvider.getEventBus()
    val trackingStatus = MutableLiveData(0)
    val isCountdownOn: MutableLiveData<Boolean> = MutableLiveData(false)
    val countdownProgress = MutableLiveData(-1L)
    val countDownMaxValue = 60000L
    private val timer = object: CountDownTimer(countDownMaxValue, 500) {
        override fun onTick(millisUntilFinished: Long) {
            countdownProgress.postValue(millisUntilFinished)
        }
        override fun onFinish() {
            isCountdownOn.postValue(false)
        }
    }
    private val sharedPreferences = application
        .getSharedPreferences("showLocationPermissionRequest", Context.MODE_PRIVATE)

    private var pitch = 0.0
    private var roll = 0.0
    val experimentalData = MutableStateFlow(ExperimentalData())


    init {
         showLocationPermissionRequest
            .postValue(sharedPreferences.getString("showLocationPermissionRequest", "true"))
        bus.register(this)
        if (currentTrackId.value == null) {
            firstComfortIndexRecordForAllExceptCurrent = comfortIndexRecordDao.getFirstComfortIndexRecordForAll()
        }
    }

    fun disableLocationPermissionRequestDialog() {
        with (sharedPreferences.edit()) {
            putString("showLocationPermissionRequest", "false")
            showLocationPermissionRequest.postValue("false")
            apply()
        }
    }

    fun enableLocationTracking() {
        if (!locationClient.isReceivingLocationUpdates()) {
            locationClient
                .getLocationUpdates(1000L)
                .catch { e -> e.printStackTrace() }
                .onEach { loc ->
                    location.postValue(loc)
                }
                .launchIn(viewModelScope)
        }
    }

    fun toggleCameraFollow() {
        if (cameraFollow) {
            disableCameraFollow()
        }
        else {
            enableCameraFollow()
        }
    }

    fun enableCameraFollow() {
        cameraFollow = true
    }

    fun disableCameraFollow() {
        cameraFollow = false
    }

    override fun onCleared() {
        super.onCleared()
        locationClient.removeLocationUpdates()
        bus.unregister(this)
    }

    fun deleteAllTracks() {
        viewModelScope.launch {
            trackRecordDao.deleteAll()
        }
    }

    @Subscribe
    fun onConnectionStatusChanged(event: ConnectionStatusChangedEvent) {
        connectionStatus.postValue(event.connectionStatus)

        if (event.connectionStatus <= 0 && trackingStatus.value == 1 && !isCountdownOn.value!!) {
            isCountdownOn.postValue(true)
            countdownProgress.postValue(countDownMaxValue)
            timer.start()
        }

        else if (event.connectionStatus > 0 && isCountdownOn.value!!) {
            isCountdownOn.postValue(false)
            timer.cancel()
            countdownProgress.postValue(-1L)
        }
    }

    @Subscribe
    fun onTrackingStatusChanged(event: TrackingStatusChangedEvent) {
        trackingStatus.postValue(event.trackingStatus)
    }

    @Subscribe
    fun onCurrentTrackIdChangedEvent(event: CurrentTrackIdChangedEvent) {
        if (event.currentTrackId != null) {
            pitch = 0.0
            roll = 0.0
            currentComfortIndexRecordsFlow = comfortIndexRecordDao
                .getRecordFlowListByTrackId(event.currentTrackId)
            firstComfortIndexRecordForAllExceptCurrent = comfortIndexRecordDao
                .getFirstComfortIndexRecordForAllExceptCurrent(event.currentTrackId)
        }
        else {
            currentComfortIndexRecordsFlow = emptyFlow<List<ComfortIndexRecord>>()
            firstComfortIndexRecordForAllExceptCurrent = comfortIndexRecordDao
                .getFirstComfortIndexRecordForAll()
        }
        currentTrackId.postValue(event.currentTrackId)
    }

//    @Subscribe
//    fun onSensorDataReceived(event: SensorDataReceivedEvent) {
//        val sensorData = event.data ?: return
//        val gyroX = sensorData.Body.arrayGyro.sumOf { it.x } / sensorData.Body.arrayGyro.count()
//        val gyroY = sensorData.Body.arrayGyro.sumOf { it.y } / sensorData.Body.arrayGyro.count()
//
//        val magX = sensorData.Body.arrayMagn.sumOf { it.x } / sensorData.Body.arrayMagn.count()
//        val magY = sensorData.Body.arrayMagn.sumOf { it.y } / sensorData.Body.arrayMagn.count()
//        val magZ = sensorData.Body.arrayMagn.sumOf { it.z } / sensorData.Body.arrayMagn.count()
//
//        val dt = 1.0 / 13.0
//        val alpha = 1.0
//
//        val pitchGyro = pitch + gyroY * dt
//        val rollGyro = roll + gyroX * dt
//
//        val pitchMag = atan2(magY, magZ) * (180.0 / Math.PI)
//        val rollMag = atan2(magX, magZ) * (180.0 / Math.PI)
//
//        pitch = alpha * pitchGyro + (1 - alpha) * (pitchMag)
//        roll = alpha * rollGyro + (1 - alpha) * (rollMag)
//
//        experimentalData.update { ExperimentalData(roll, pitch) }
//
//    }
//    @Subscribe
//    fun onExperimentalDataUpdated(event: ExperimentalDataUpdatedEvent) {
//        experimentalData.update {
//            event.data
//        }
//    }

}