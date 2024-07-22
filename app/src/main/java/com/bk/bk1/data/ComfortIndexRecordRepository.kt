package com.bk.bk1.data

import com.bk.bk1.models.ComfortIndexRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ComfortIndexRecordRepository @Inject constructor(private val trackDatabase: TrackDatabase) {
    private val comfortIndexRecordDao = trackDatabase.comfortIndexRecordDao

    suspend fun upsertRecord(comfortIndexRecord: ComfortIndexRecord) {
        comfortIndexRecordDao.upsertRecord(comfortIndexRecord)
    }

    suspend fun updateRecord(comfortIndexRecord: ComfortIndexRecord) {
        comfortIndexRecordDao.updateRecord(comfortIndexRecord)
    }

    fun getRecordFlowListByTrackId(trackRecordId: Int): Flow<List<ComfortIndexRecord>> {
        return comfortIndexRecordDao.getRecordFlowListByTrackId(trackRecordId)
    }

    suspend fun getRecordListByTrackId(trackRecordId: Int): List<ComfortIndexRecord> {
        return comfortIndexRecordDao.getRecordListByTrackId(trackRecordId)
    }

    fun getAllRecords(): Flow<List<ComfortIndexRecord>> {
        return comfortIndexRecordDao.getAllRecords()
    }

    fun getFirstComfortIndexRecordForAllExceptCurrent(currentTrackId: Int): Flow<List<ComfortIndexRecord>> {
        return comfortIndexRecordDao.getFirstComfortIndexRecordForAllExceptCurrent(currentTrackId)
    }

    fun getFirstComfortIndexRecordForAll(): Flow<List<ComfortIndexRecord>> {
        return comfortIndexRecordDao.getFirstComfortIndexRecordForAll()
    }

    suspend fun getComfortIndexRecordCount(trackId: Int): Int {
        return comfortIndexRecordDao.getComfortIndexRecordCount(trackId)
    }
}