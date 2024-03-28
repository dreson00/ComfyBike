package com.bk.bk1.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bk.bk1.data.ComfortIndexRecordDao
import com.bk.bk1.data.TrackRecordDao
import com.bk.bk1.models.ComfortIndexRecord
import com.bk.bk1.models.TrackRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackDetailScreenViewModel @Inject constructor(
    private val trackRecordDao: TrackRecordDao,
    private val comfortIndexRecordDao: ComfortIndexRecordDao
) : ViewModel() {

    fun getTrackRecordById(trackId: Int): Flow<TrackRecord?>  {
        return trackRecordDao.getTrackRecordById(trackId)
    }

    fun getComfortIndexRecordsByTrackId(trackId: Int): Flow<List<ComfortIndexRecord>> {
        return comfortIndexRecordDao.getRecordsByTrackId(trackId)
    }

    fun deleteTrackRecord(trackRecord: TrackRecord) {
        viewModelScope.launch {
            trackRecordDao.deleteTrackRecord(trackRecord)
        }
    }
}