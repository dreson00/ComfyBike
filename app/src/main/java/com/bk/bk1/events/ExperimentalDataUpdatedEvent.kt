package com.bk.bk1.events

import com.bk.bk1.models.ExperimentalData

data class ExperimentalDataUpdatedEvent(
    val data: ExperimentalData
)