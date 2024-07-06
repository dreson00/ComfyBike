package com.bk.bk1.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bk.bk1.data.ComfortIndexRecordRepository
import com.bk.bk1.data.TrackRecordRepository
import com.bk.bk1.enums.ExportCsvStatus
import com.bk.bk1.events.CurrentTrackIdChangedEvent
import com.bk.bk1.models.TrackRecord
import com.bk.bk1.states.TrackListScreenState
import com.bk.bk1.utilities.TrackRecordExportManager
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackListScreenViewModel @Inject constructor(
    private val bus: Bus,
    private val trackRecordExportManager: TrackRecordExportManager,
    private val trackRecordRepository: TrackRecordRepository,
    private val comfortIndexRecordRepository: ComfortIndexRecordRepository
) : ViewModel() {
    val state = MutableStateFlow(TrackListScreenState())

    init {
        bus.register(this)
        viewModelScope.launch(Dispatchers.IO) {
            state.update {
                it.copy(
                    trackRecords = trackRecordRepository.getTrackRecordList()
                )
            }
        }
    }

    fun deleteTrack(trackRecord: TrackRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            trackRecordRepository.deleteTrackRecord(trackRecord)
            state.update {
                it.copy(trackRecords = trackRecordRepository.getTrackRecordList())
            }
        }
    }

    fun setShowExternalStorageRequest(value: Boolean) {
        state.update {
            it.copy(
                showExternalStorageRequest = value
            )
        }
    }

    fun resetExportCsvStatus() {
        state.update {
            it.copy(
                exportCsvStatus = ExportCsvStatus.NOT_SAVING
            )
        }
    }

    fun saveCiRecordsAsCsv(tracKRecord: TrackRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            val ciRecordList = comfortIndexRecordRepository.getRecordListByTrackId(tracKRecord.id)
            val status = trackRecordExportManager
                .saveCiListAsCsv(ciRecordList, "track_${tracKRecord.id}")
            state.update {
                it.copy(
                    exportCsvStatus = status
                )
            }
        }
    }

    override fun onCleared() {
        bus.unregister(this)
        super.onCleared()
    }

    @Subscribe
    fun onCurrentTrackIdChanged(event: CurrentTrackIdChangedEvent) {
        state.update {
            it.copy(
                currentTrackId = event.currentTrackId
            )
        }
    }
}