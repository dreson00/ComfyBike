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
    fun getTrackRecords(): Flow<List<TrackRecord>>

    @Query("SELECT * FROM trackrecord WHERE id = :trackId")
    fun getTrackRecordById(trackId: Int) : Flow<TrackRecord?>

    @Query("DELETE FROM trackRecord")
    suspend fun deleteAll()

    @Query("DELETE FROM TrackRecord WHERE id = :trackId")
    suspend fun deleteTrackRecord(trackId: Int)


}