package com.bk.bk1.data

import com.bk.bk1.models.TrackRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TrackRecordRepository @Inject constructor(private val trackDatabase: TrackDatabase) {
    private val trackRecordDao = trackDatabase.trackRecordDao

    suspend fun upsertTrackRecord(trackRecord: TrackRecord): Long {
        return trackRecordDao.upsertTrackRecord(trackRecord)
    }

    suspend fun deleteTrackRecord(trackRecord: TrackRecord) {
        trackRecordDao.deleteTrackRecord(trackRecord)
    }

    fun getTrackRecordList(): List<TrackRecord> {
        return trackRecordDao.getTrackRecordList()
    }

    fun getTrackRecordFlowById(trackId: Int): Flow<TrackRecord?> {
        return trackRecordDao.getTrackRecordFlowById(trackId)
    }

    suspend fun getTrackRecordById(trackId: Int): TrackRecord? {
        return trackRecordDao.getTrackRecordById(trackId)
    }

    suspend fun deleteTrackRecord(trackId: Int) {
        trackRecordDao.deleteTrackRecord(trackId)
    }
}