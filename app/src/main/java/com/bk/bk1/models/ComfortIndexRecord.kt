package com.bk.bk1.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ForeignKey(
    entity = TrackRecord::class,
    parentColumns = arrayOf("id"),
    childColumns = arrayOf("trackRecordId"),
    onDelete = ForeignKey.CASCADE
)])
data class ComfortIndexRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo var comfortIndex: Float,
    val bicycleSpeed: Float,
    val trackRecordId: Int,
    val latitude: Double,
    val longitude: Double
)