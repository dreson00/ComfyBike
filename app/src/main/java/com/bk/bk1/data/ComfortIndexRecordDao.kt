package com.bk.bk1.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.bk.bk1.models.ComfortIndexRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ComfortIndexRecordDao {

    @Upsert
    suspend fun upsertRecord(comfortIndexRecord: ComfortIndexRecord)

    @Query("SELECT * FROM comfortIndexRecord WHERE id = :trackRecordId")
    fun getRecordsByTrackId(trackRecordId: Int): Flow<List<ComfortIndexRecord>>

    @Query("SELECT * FROM comfortindexrecord")
    fun getAllRecords(): Flow<List<ComfortIndexRecord>>
}