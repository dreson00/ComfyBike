package com.bk.bk1.events

import com.bk.bk1.enums.TrackingStatus

data class TrackingStatusChangedEvent(
    val trackingStatus: TrackingStatus
)