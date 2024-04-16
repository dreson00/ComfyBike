package com.bk.bk1.viewModels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bk.bk1.data.ComfortIndexRecordDao
import com.bk.bk1.enums.ImageSavingStatus
import com.bk.bk1.events.MapScreenshotterScreenEvent
import com.bk.bk1.models.ComfortIndexRecord
import com.bk.bk1.states.MapScreenshotterScreenState
import com.bk.bk1.utilities.ExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.floor

@HiltViewModel
class MapScreenshotterScreenViewModel @Inject constructor(
    private val exportManager: ExportManager,
    val comfortIndexRecordDao: ComfortIndexRecordDao
) : ViewModel() {
    val state = MutableStateFlow(MapScreenshotterScreenState())
    private var _comfortIndexRecords = emptyList<ComfortIndexRecord>()

    fun onEvent(event: MapScreenshotterScreenEvent) {
        when (event) {
            is MapScreenshotterScreenEvent.SetTrackId -> {
                setTrackId(event.trackId)
            }
            MapScreenshotterScreenEvent.HideUI -> {
                state.update { it.copy(captureMode = true) }
            }
            is MapScreenshotterScreenEvent.FilterRecordsBySpeed -> {
                filterRecordsBySpeedRange(event.speedRange)
            }
            MapScreenshotterScreenEvent.HideSettings -> {
                state.update { it.copy(showSettings = false) }
            }
            is MapScreenshotterScreenEvent.SaveImage -> {
                saveImage(event.bitmap, event.trackId)
            }
            is MapScreenshotterScreenEvent.SetImageSavingStatus -> {
                state.update { it.copy(imageSavingStatus = event.imageSavingStatus) }
            }
            MapScreenshotterScreenEvent.ToggleSettingsVisible -> {
                state.update { it.copy(showSettings = !state.value.showSettings) }
            }
            MapScreenshotterScreenEvent.ToggleSpeedFilterWatermark -> {
                state.update { it.copy(showSpeedFilterWatermark = !state.value.showSpeedFilterWatermark) }
            }
        }
    }

    private fun setTrackId(trackId: Int) {
        viewModelScope.launch {
            _comfortIndexRecords = comfortIndexRecordDao.getRecordListByTrackId(trackId)
            val speedMin = floor(_comfortIndexRecords.minBy { it.bicycleSpeed }.bicycleSpeed)
            val speedMax = ceil(_comfortIndexRecords.maxBy { it.bicycleSpeed }.bicycleSpeed)
            state.update {
                it.copy(
                    trackId = trackId,
                    comfortIndexRecords = _comfortIndexRecords,
                    speedMin = speedMin,
                    speedMax = speedMax,
                    speedFilterRange = speedMin..speedMax
                )
            }

        }
    }

    private fun filterRecordsBySpeedRange(speedRange: ClosedFloatingPointRange<Float>) {
        val comfortIndexRecords = _comfortIndexRecords.filter {
//            val minSpeed = floor(speedRange.start.toDouble())
//            val maxSpeed = ceil(speedRange.endInclusive.toDouble())
            it.bicycleSpeed >= speedRange.start && it.bicycleSpeed <= speedRange.endInclusive
        }
        state.update {
            it.copy(
                comfortIndexRecords = comfortIndexRecords,
                speedFilterRange = speedRange
            )
        }
    }

    private fun saveImage(bitmap: Bitmap, trackId: Int) {
        val exportStatus = exportManager.saveBitmapAsPng(bitmap, "track_$trackId")
        if (exportStatus == 0) {
            state.update { it.copy(imageSavingStatus = ImageSavingStatus.SUCCESS) }
        }
        else if (exportStatus == 1) {
            state.update { it.copy(imageSavingStatus = ImageSavingStatus.ERROR) }
        }
    }
}