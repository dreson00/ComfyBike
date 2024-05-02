package com.bk.bk1.viewModels


import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bk.bk1.data.ComfortIndexRecordDao
import com.bk.bk1.enums.SensorConnectionStatus
import com.bk.bk1.enums.TrackingStatus
import com.bk.bk1.events.ConnectionStatusChangedEvent
import com.bk.bk1.events.CurrentTrackIdChangedEvent
import com.bk.bk1.events.TrackingStatusChangedEvent
import com.bk.bk1.states.MainMapScreenState
import com.bk.bk1.utilities.BluetoothStateUpdater
import com.bk.bk1.utilities.LocationClient
import com.bk.bk1.utilities.LocationStateUpdater
import com.leinardi.android.speeddial.compose.SpeedDialState
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@SuppressLint("MissingPermission")
@HiltViewModel
class MainMapScreenViewModel @Inject constructor(
    private val application: Application,
    private val bus: Bus,
    private val locationClient: LocationClient,
    private val comfortIndexRecordDao: ComfortIndexRecordDao,
    private val bluetoothStateUpdater: BluetoothStateUpdater,
    private val locationStateUpdater: LocationStateUpdater
) : ViewModel() {

    private val _state = MutableStateFlow(MainMapScreenState())
    private val _firstComfortIndexRecordForAllExceptCurrentFlow = _state.flatMapLatest {
        when(it.currentTrackId) {
            null -> comfortIndexRecordDao.getFirstComfortIndexRecordForAll()
            else -> comfortIndexRecordDao.getFirstComfortIndexRecordForAllExceptCurrent(it.currentTrackId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _currentComfortIndexRecordsFlow = _state.flatMapLatest {
        when(it.currentTrackId) {
            null -> emptyFlow()
            else -> comfortIndexRecordDao.getRecordFlowListByTrackId(it.currentTrackId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _isBluetoothAdapterOn = bluetoothStateUpdater.bluetoothState
    private val _isLocationEnabled = locationStateUpdater.locationState

    val state = combine(
        _state,
        _isBluetoothAdapterOn,
        _isLocationEnabled,
        _firstComfortIndexRecordForAllExceptCurrentFlow,
        _currentComfortIndexRecordsFlow
    ) { state,
        isBluetoothAdapterOn,
        isLocationEnabled,
        firstComfortIndexRecordForAllExceptCurrent,
        currentComfortIndexRecordsFlow
        ->

        state.copy(
            isBluetoothAdapterOn = isBluetoothAdapterOn,
            isLocationEnabled = isLocationEnabled,
            firstComfortIndexRecordForAllTracks = firstComfortIndexRecordForAllExceptCurrent,
            currentComfortIndexRecords = currentComfortIndexRecordsFlow
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), MainMapScreenState())


    private val countDownMaxValue = 60000L
    private val timer = object: CountDownTimer(countDownMaxValue, 500) {
        override fun onTick(millisUntilFinished: Long) {
            _state.update { it.copy(countdownProgress = millisUntilFinished) }
        }
        override fun onFinish() {
            _state.update { it.copy(isCountdownOn = false) }
        }
    }
    private val sharedPreferences = application
        .getSharedPreferences("showLocationPermissionRequest", Context.MODE_PRIVATE)

    init {
        bus.register(this)
        _state.update {
            it.copy(
                showLocationPermissionRequestPreference = sharedPreferences
                    .getString("showLocationPermissionRequest", "true")
            )
        }
    }

    fun disableLocationPermissionRequestDialog() {
        with (sharedPreferences.edit()) {
            putString("showLocationPermissionRequest", "false")
            _state.update { it.copy(showLocationPermissionRequestPreference = "false") }
            apply()
        }
    }

    fun setShowLocationPermissionRequest(value: Boolean) {
        _state.update { it.copy(showLocationPermissionRequest = value) }
    }

    fun setShowBluetoothScanRequest(value: Boolean) {
        _state.update { it.copy(showBluetoothScanRequest = value) }
    }

    fun enableLocationTracking() {
        if (!locationClient.isReceivingLocationUpdates()) {
            locationClient
                .getLocationUpdates(1000L)
                .catch { e -> e.printStackTrace() }
                .onEach { loc ->
                    _state.update { it.copy(location = loc) }
                }
                .launchIn(viewModelScope)
        }
    }

    fun setSpeedDialMenuState(value: SpeedDialState) {
        _state.update { it.copy(speedDialState = value) }
    }

    fun toggleSpeedDialMenuState() {
        _state.update {
            it.copy(
                speedDialState = if (state.value.speedDialState == SpeedDialState.Collapsed) {
                    SpeedDialState.Expanded
                } else {
                    SpeedDialState.Collapsed
                }
            )
        }
    }

    fun setSpeedDialOverlayVisible(value: Boolean) {
        _state.update { it.copy(speedDialOverlayVisible = value) }
    }

    fun toggleCameraFollow() {
        _state.update { it.copy(cameraFollow = !_state.value.cameraFollow) }
    }

    fun enableCameraFollow() {
        _state.update { it.copy(cameraFollow = true) }
    }

    fun disableCameraFollow() {
        _state.update { it.copy(cameraFollow = false) }
    }

    override fun onCleared() {
        super.onCleared()
        locationClient.removeLocationUpdates()
        bus.unregister(this)
    }

    @Subscribe
    fun onConnectionStatusChanged(event: ConnectionStatusChangedEvent) {
        _state.update { it.copy(connectionStatus = event.connectionStatus) }

        if (event.connectionStatus <= SensorConnectionStatus.DISCONNECTED
            && _state.value.trackingStatus == TrackingStatus.TRACKING
            && !_state.value.isCountdownOn) {
            _state.update { it.copy(isCountdownOn = true) }
            _state.update { it.copy(countdownProgress = countDownMaxValue) }
            timer.start()
        }

        else if (event.connectionStatus > SensorConnectionStatus.DISCONNECTED && _state.value.isCountdownOn) {
            _state.update { it.copy(isCountdownOn = false) }
            timer.cancel()
            _state.update { it.copy(countdownProgress = -1L) }
        }
    }

    @Subscribe
    fun onTrackingStatusChanged(event: TrackingStatusChangedEvent) {
        _state.update { it.copy(trackingStatus = event.trackingStatus) }
    }

    @Subscribe
    fun onCurrentTrackIdChangedEvent(event: CurrentTrackIdChangedEvent) {
        _state.update { it.copy(currentTrackId = event.currentTrackId) }
    }
}