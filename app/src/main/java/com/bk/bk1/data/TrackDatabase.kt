package com.bk.bk1.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bk.bk1.models.ComfortIndexRecord
import com.bk.bk1.models.TrackRecord

@Database(
    entities = [TrackRecord::class, ComfortIndexRecord::class],
    version = 1
)
abstract class TrackDatabase : RoomDatabase() {

    abstract val trackRecordDao: TrackRecordDao
    abstract val comfortIndexRecordDao: ComfortIndexRecordDao

    companion object {

        @Volatile
        private var INSTANCE: TrackDatabase? = null
        fun getDatabase(context: Context): TrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrackDatabase::class.java,
                    "MyRemoteDatabase"
                ).fallbackToDestructiveMigration().build()

                INSTANCE = instance

                instance
            }
        }
    }
}