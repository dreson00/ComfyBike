package com.bk.bk1.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.bk.bk1.models.TrackRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackRecordDao {

    @Upsert
    suspend fun upsertTrackRecord(trackRecord: TrackRecord): Long

    @Delete
    suspend fun deleteTrackRecord(trackRecord: TrackRecord)

    @Query("SELECT * FROM trackRecord")
    fun getTrackRecordList(): List<TrackRecord>

    @Query("SELECT * FROM trackrecord WHERE id = :trackId")
    fun getTrackRecordFlowById(trackId: Int): Flow<TrackRecord?>

    @Query("SELECT * FROM trackrecord WHERE id = :trackId")
    suspend fun getTrackRecordById(trackId: Int): TrackRecord?

    @Query("DELETE FROM TrackRecord WHERE id = :trackId")
    suspend fun deleteTrackRecord(trackId: Int)
}