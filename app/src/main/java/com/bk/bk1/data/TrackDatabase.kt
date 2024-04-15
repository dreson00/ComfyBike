package com.bk.bk1.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bk.bk1.models.ComfortIndexRecord
import com.bk.bk1.models.TrackRecord

@Database(
    entities = [TrackRecord::class, ComfortIndexRecord::class],
    version = 3
)
abstract class TrackDatabase : RoomDatabase() {
    abstract val trackRecordDao: TrackRecordDao
    abstract val comfortIndexRecordDao: ComfortIndexRecordDao
}