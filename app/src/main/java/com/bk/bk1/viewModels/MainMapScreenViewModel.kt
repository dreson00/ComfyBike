package com.bk.bk1.viewModels


import android.annotation.SuppressLint
import android.location.Location
import android.os.CountDownTimer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bk.bk1.data.ComfortIndexRecordDao
import com.bk.bk1.data.TrackRecordDao
import com.bk.bk1.events.ConnectionStatusChangedEvent
import com.bk.bk1.events.TrackingStatusChangedEvent
import com.bk.bk1.utilities.BusProvider
import com.bk.bk1.utilities.LocationClient
import com.squareup.otto.Subscribe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("MissingPermission")
@HiltViewModel
class MainMapScreenViewModel @Inject constructor(
    private val locationClient: LocationClient,
    private val trackRecordDao: TrackRecordDao,
    private val comfortIndexRecordDao: ComfortIndexRecordDao
) : ViewModel() {

    var cameraFollow by mutableStateOf(true)
    var location = MutableLiveData<Location>(null)

    var comfortIndexRecords = comfortIndexRecordDao.getAllRecords()
    val connectionStatus = MutableLiveData(0)
    private val bus = BusProvider.getEventBus()
    val trackingStatus = MutableLiveData(0)
    val isCountdownOn: MutableLiveData<Boolean> = MutableLiveData(false)
    val countdownProgress = MutableLiveData(-1L)
    val countDownMaxValue = 15000L
    private val timer = object: CountDownTimer(countDownMaxValue, 500) {
        override fun onTick(millisUntilFinished: Long) {
            countdownProgress.postValue(millisUntilFinished)
        }
        override fun onFinish() {
            isCountdownOn.postValue(false)
        }
    }



    init {
        locationClient
            .getLocationUpdates(1000L)
            .catch { e -> e.printStackTrace() }
            .onEach { loc ->
                location.postValue(loc)
            }
            .launchIn(viewModelScope)
        bus.register(this)
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

        if (event.connectionStatus <= 0 && !isCountdownOn.value!!) {
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

}