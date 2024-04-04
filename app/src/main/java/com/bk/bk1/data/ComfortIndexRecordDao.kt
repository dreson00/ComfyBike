package com.bk.bk1.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.bk.bk1.models.ComfortIndexRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ComfortIndexRecordDao {

    @Upsert
    suspend fun upsertRecord(comfortIndexRecord: ComfortIndexRecord)

    @Update
    suspend fun updateRecord(comfortIndexRecord: ComfortIndexRecord)

    @Query("SELECT * FROM comfortIndexRecord WHERE trackRecordId = :trackRecordId")
    fun getRecordsByTrackId(trackRecordId: Int): Flow<List<ComfortIndexRecord>>

    @Query("SELECT * FROM comfortindexrecord")
    fun getAllRecords(): Flow<List<ComfortIndexRecord>>

    @Query("SELECT * FROM ComfortIndexRecord WHERE trackRecordId NOT IN (:currentTrackId) GROUP BY trackRecordId")
    fun getFirstComfortIndexRecordForAllExceptCurrent(currentTrackId: Int): Flow<List<ComfortIndexRecord>>

    @Query("SELECT * FROM ComfortIndexRecord GROUP BY trackRecordId")
    fun getFirstComfortIndexRecordForAll(): Flow<List<ComfortIndexRecord>>

    @Query("SELECT COUNT(*) FROM ComfortIndexRecord WHERE trackRecordId = :trackId")
    fun getComfortIndexRecordCount(trackId: Int): Int


}