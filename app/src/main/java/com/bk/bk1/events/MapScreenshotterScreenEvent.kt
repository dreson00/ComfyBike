package com.bk.bk1.events

import android.graphics.Bitmap
import com.bk.bk1.enums.ImageSavingStatus

sealed interface MapScreenshotterScreenEvent {
    data class SetTrackId(val trackId: Int): MapScreenshotterScreenEvent
    object ToggleSpeedFilterWatermark: MapScreenshotterScreenEvent
    object ToggleSettingsVisible: MapScreenshotterScreenEvent
    object HideSettings: MapScreenshotterScreenEvent
    object HideUI: MapScreenshotterScreenEvent
    data class FilterRecordsBySpeed(val speedRange: ClosedFloatingPointRange<Float>): MapScreenshotterScreenEvent
    data class SaveImage(val bitmap: Bitmap, val trackId: Int): MapScreenshotterScreenEvent
    data class SetImageSavingStatus(val imageSavingStatus: ImageSavingStatus): MapScreenshotterScreenEvent
}