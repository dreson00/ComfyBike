package com.bk.bk1.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bk.bk1.data.ComfortIndexRecordDao
import com.bk.bk1.data.TrackRecordDao
import com.bk.bk1.events.CurrentTrackIdChangedEvent
import com.bk.bk1.models.TrackRecord
import com.bk.bk1.utilities.BusProvider
import com.bk.bk1.utilities.ExportManager
import com.squareup.otto.Subscribe
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
    val currentTrackId = MutableLiveData<Int?>(null)
    private val bus = BusProvider.getEventBus()

    init {
        bus.register(this)
    }


    fun deleteTrack(trackRecord: TrackRecord) {
        viewModelScope.launch {
            trackRecordDao.deleteTrackRecord(trackRecord)
        }
    }

    override fun onCleared() {
        super.onCleared()
        bus.unregister(this)
    }

    suspend fun saveCiRecordsAsCsv(tracKRecord: TrackRecord): Int {
        val ciRecordList = comfortIndexRecordDao
            .getRecordFlowListByTrackId(tracKRecord.id)
            .first()
        return exportManager.saveCiListAsCsv(ciRecordList, "track_${tracKRecord.id}")
    }

    @Subscribe
    fun onCurrentTrackIdChanged(event: CurrentTrackIdChangedEvent) {
        currentTrackId.postValue(event.currentTrackId)
    }
}