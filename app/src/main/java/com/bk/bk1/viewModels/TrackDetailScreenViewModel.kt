package com.bk.bk1.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bk.bk1.data.ComfortIndexRecordRepository
import com.bk.bk1.data.TrackRecordRepository
import com.bk.bk1.models.ComfortIndexRecord
import com.bk.bk1.models.TrackRecord
import com.bk.bk1.states.TrackDetailScreenState
import com.bk.bk1.utilities.parseFromDbFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackDetailScreenViewModel @Inject constructor(
    private val trackRecordRepository: TrackRecordRepository,
    private val comfortIndexRecordRepository: ComfortIndexRecordRepository
) : ViewModel() {

    val state = MutableStateFlow(TrackDetailScreenState())
    private var _comfortIndexRecords = emptyList<ComfortIndexRecord>()
    private lateinit var _trackRecord: TrackRecord

    fun initTrackData(trackId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _comfortIndexRecords = comfortIndexRecordRepository.getRecordListByTrackId(trackId)
            val speedMin = _comfortIndexRecords.minBy { it.bicycleSpeed }.bicycleSpeed
            val speedMax = _comfortIndexRecords.maxBy { it.bicycleSpeed }.bicycleSpeed
            filterRecordsBySpeedRange(speedMin..speedMax)

            trackRecordRepository.getTrackRecordById(trackId)?.let { trackRecord ->
                _trackRecord = trackRecord
                state.update {
                    it.copy(
                        speedMin = speedMin,
                        speedMax = speedMax,
                        trackId = trackId,
                        comfortIndexRecords = _comfortIndexRecords,
                        recordTime = parseFromDbFormat(trackRecord.time),
                        dataLoaded = true
                    )
                }
            }
        }
    }

    fun filterRecordsBySpeedRange(speedRange: ClosedFloatingPointRange<Float>) {
        val comfortIndexRecords = _comfortIndexRecords.filter {
            it.bicycleSpeed in speedRange
        }
        val recordCount = comfortIndexRecords.count()

        if (comfortIndexRecords.isNotEmpty()) {
            val minimumCi = comfortIndexRecords.minBy { it.comfortIndex }.comfortIndex
            val maximumCi = comfortIndexRecords.maxBy { it.comfortIndex }.comfortIndex
            val averageCi = (
                    comfortIndexRecords.sumOf { it.comfortIndex.toDouble() } / recordCount
                    )

            val medianCi =
                if (recordCount % 2 == 0) {
                    val midIndex1 = recordCount / 2
                    val midIndex2 = midIndex1 - 1
                    (comfortIndexRecords[midIndex1].comfortIndex
                            + comfortIndexRecords[midIndex2].comfortIndex) / 2
                } else {
                    val midIndex = recordCount / 2
                    comfortIndexRecords[midIndex].comfortIndex
                }

            state.update {
                it.copy(
                    comfortIndexRecords = comfortIndexRecords,
                    recordCount = recordCount,
                    ciMin = minimumCi.toDouble(),
                    ciMax = maximumCi.toDouble(),
                    ciAvg = averageCi,
                    ciMedian = medianCi.toDouble()
                )
            }
        }
        else {
            state.update {
                it.copy(
                    comfortIndexRecords = emptyList(),
                    recordCount = recordCount
                )
            }
        }


    }
}