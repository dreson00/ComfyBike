package com.bk.bk1.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TrackRecord (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val time: String
)