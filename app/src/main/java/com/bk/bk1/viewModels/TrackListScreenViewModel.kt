package com.bk.bk1.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bk.bk1.data.ComfortIndexRecordDao
import com.bk.bk1.data.TrackRecordDao
import com.bk.bk1.models.TrackRecord
import com.bk.bk1.utilities.ExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackListScreenViewModel @Inject constructor(
    private val exportManager: ExportManager,
    private val trackRecordDao: TrackRecordDao,
    private val comfortIndexRecordDao: ComfortIndexRecordDao
) : ViewModel() {
    val tracks = trackRecordDao.getTrackRecords().asLiveData()


    fun deleteTrack(trackRecord: TrackRecord) {
        viewModelScope.launch {
            trackRecordDao.deleteTrackRecord(trackRecord)
        }
    }

    suspend fun saveCiRecordsAsCsv(tracKRecord: TrackRecord): Int {
        val ciRecordList = comfortIndexRecordDao
            .getRecordsByTrackId(tracKRecord.id)
            .first()
        return exportManager.saveCiListAsCsv(ciRecordList, "track_${tracKRecord.id}")
    }
}