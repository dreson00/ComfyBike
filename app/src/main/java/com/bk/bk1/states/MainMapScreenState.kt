package com.bk.bk1.states

import android.location.Location
import com.bk.bk1.enums.SensorConnectionStatus
import com.bk.bk1.enums.TrackingStatus
import com.bk.bk1.models.ComfortIndexRecord
import com.leinardi.android.speeddial.compose.SpeedDialState

data class MainMapScreenState(
    val cameraFollow: Boolean = true,
    val speedDialState: SpeedDialState = SpeedDialState.Collapsed,
    val speedDialOverlayVisible: Boolean = false,
    val currentTrackId: Int? = null,
    val showLocationPermissionRequest: Boolean = false,
    val showLocationPermissionRequestPreference: String? = null,
    val showBluetoothScanRequest: Boolean = false,
    val location: Location? = null,
    val connectionStatus: SensorConnectionStatus = SensorConnectionStatus.DISCONNECTED,
    val isBluetoothAdapterOn: Boolean = true,
    val isLocationEnabled: Boolean = false,
    val trackingStatus: TrackingStatus = TrackingStatus.NOT_TRACKING,
    val isCountdownOn: Boolean = false,
    val countdownProgress: Long = -1L,
    val currentComfortIndexRecords: List<ComfortIndexRecord> = emptyList(),
    val firstComfortIndexRecordForAllTracks: List<ComfortIndexRecord> = emptyList(),
    )