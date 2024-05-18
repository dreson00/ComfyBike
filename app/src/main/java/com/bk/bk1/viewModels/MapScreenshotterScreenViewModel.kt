package com.bk.bk1.viewModels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bk.bk1.data.ComfortIndexRecordRepository
import com.bk.bk1.enums.ImageSavingStatus
import com.bk.bk1.models.ComfortIndexRecord
import com.bk.bk1.states.MapScreenshotterScreenState
import com.bk.bk1.utilities.ExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapScreenshotterScreenViewModel @Inject constructor(
    private val exportManager: ExportManager,
    private val comfortIndexRecordRepository: ComfortIndexRecordRepository
) : ViewModel() {
    val state = MutableStateFlow(MapScreenshotterScreenState())
    private var _comfortIndexRecords = emptyList<ComfortIndexRecord>()

    fun hideUI() {
        state.update { it.copy(hideUI = true) }
    }

    fun beginCapture() {
        state.update { it.copy(beginCapture = true) }
    }

    fun setImageSavingStatus(imageSavingStatus: ImageSavingStatus) {
        state.update { it.copy(imageSavingStatus = imageSavingStatus) }
    }

    fun toggleSettingsVisible() {
        state.update { it.copy(showSettings = !state.value.showSettings) }
    }

    fun toggleSpeedFilterWatermark() {
        state.update { it.copy(showSpeedFilterWatermark = !state.value.showSpeedFilterWatermark) }
    }

    fun initTrackData(trackId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _comfortIndexRecords = comfortIndexRecordRepository.getRecordListByTrackId(trackId)
            val speedMin = _comfortIndexRecords.minBy { it.bicycleSpeed }.bicycleSpeed
            val speedMax = _comfortIndexRecords.maxBy { it.bicycleSpeed }.bicycleSpeed
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

    fun filterRecordsBySpeedRange(speedRange: ClosedFloatingPointRange<Float>) {
        val comfortIndexRecords = _comfortIndexRecords.filter {
            it.bicycleSpeed in speedRange
        }
        state.update {
            it.copy(
                comfortIndexRecords = comfortIndexRecords,
                speedFilterRange = speedRange
            )
        }
    }

    fun saveImage(bitmap: Bitmap, trackId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val exportStatus = exportManager.saveBitmapAsPng(bitmap, "track_$trackId")
            if (exportStatus == 0) {
                setImageSavingStatus(ImageSavingStatus.SUCCESS)
            }
            else if (exportStatus == 1) {
                setImageSavingStatus(ImageSavingStatus.ERROR)
            }
        }
    }
}