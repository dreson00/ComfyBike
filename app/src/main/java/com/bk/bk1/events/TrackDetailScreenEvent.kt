package com.bk.bk1.events

sealed interface TrackDetailScreenEvent {
    data class SetTrackId(val trackId: Int): TrackDetailScreenEvent
    data class FilterRecordsBySpeed(val speedRange: ClosedFloatingPointRange<Float>): TrackDetailScreenEvent
}