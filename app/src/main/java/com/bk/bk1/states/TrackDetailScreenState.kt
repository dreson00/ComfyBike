package com.bk.bk1.states

import com.bk.bk1.models.ComfortIndexRecord

data class TrackDetailScreenState(
    val trackId: Int = 0,
    val comfortIndexRecords: List<ComfortIndexRecord> = emptyList(),
    val recordTime: String = String(),
    val recordCount: Int = 0,
    val ciMin: Double = 0.0,
    val ciMax: Double = 0.0,
    val ciAvg: Double = 0.0,
    val ciMedian: Double = 0.0,
    val speedMin: Float = 0f,
    val speedMax: Float = 30f,
    val dataLoaded: Boolean = false
)