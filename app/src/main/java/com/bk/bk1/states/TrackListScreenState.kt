package com.bk.bk1.states

import com.bk.bk1.enums.ExportCsvStatus
import com.bk.bk1.models.TrackRecord

data class TrackListScreenState(
    val trackRecords: List<TrackRecord> = emptyList(),
    val currentTrackId: Int? = null,
    val showExternalStorageRequest: Boolean = false,
    val exportCsvStatus: ExportCsvStatus = ExportCsvStatus.NOT_SAVING
)