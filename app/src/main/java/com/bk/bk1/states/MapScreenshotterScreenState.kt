package com.bk.bk1.states

import com.bk.bk1.enums.ImageSavingStatus
import com.bk.bk1.models.ComfortIndexRecord

data class MapScreenshotterScreenState(
    val trackId: Int = 0,
    val comfortIndexRecords: List<ComfortIndexRecord> = emptyList(),
    val showSpeedFilterWatermark: Boolean = true,
    val speedFilterRange: ClosedFloatingPointRange<Float> = 0f..30f,
    val speedMin: Float = 0f,
    val speedMax: Float = 30f,
    val showSettings: Boolean = false,
    val hideUI: Boolean = false,
    val beginCapture: Boolean = false,
    val imageSavingStatus: ImageSavingStatus = ImageSavingStatus.IDLE

)