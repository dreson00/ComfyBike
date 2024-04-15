package com.bk.bk1.viewModels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.bk.bk1.data.ComfortIndexRecordDao
import com.bk.bk1.models.ComfortIndexRecord
import com.bk.bk1.utilities.ExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MapScreenshotterScreenViewModel @Inject constructor(
    private val exportManager: ExportManager,
    val comfortIndexRecordDao: ComfortIndexRecordDao
) : ViewModel() {
    fun getComfortIndexRecordsByTrackId(
        trackId: Int
    ): Flow<List<ComfortIndexRecord>> = comfortIndexRecordDao.getRecordFlowListByTrackId(trackId)

    fun saveImage(bitmap: Bitmap, trackId: Int): Int {
        return exportManager.saveBitmapAsPng(bitmap, "track_$trackId")
    }
}