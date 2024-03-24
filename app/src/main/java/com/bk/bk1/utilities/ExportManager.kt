package com.bk.bk1.utilities

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import com.bk.bk1.models.ComfortIndexRecord
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ExportManager @Inject constructor(
    private val appContext: Context
) {
    private val filesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

    fun saveBitmapAsPng(bitmap: Bitmap, fileName: String): Int {
        val folder = getOrCreateFolder()
        folder?.let {
            val file = createFile(folder, fileName, "png")
            val outputStream = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

            outputStream.flush()
            outputStream.close()
            return 0
        }
        return 1
    }

    fun saveCiListAsCsv(records: List<ComfortIndexRecord>, fileName: String): Int {
        val folder = getOrCreateFolder()
        folder?.let {
            val file = createFile(folder, fileName, "csv")
            file.bufferedWriter().use { out ->
                out.write("comfortIndex,latitude,longitude\n")
                records.forEach { record ->
                    out.write("${record.comfortIndex},${record.latitude},${record.longitude}\n")
                }
            }
            return 0
        }
        return 1
    }

    private fun getOrCreateFolder(): File? {
        if (filesDir != null) {
            val folder = File(filesDir, "ComfyBike")
            if (!folder.exists()) {
                folder.mkdir()
            }
            return folder
        }
        return null
    }

    private fun createFile(folder: File, fileName: String, extensionNoDot: String): File {
        var file = File(folder, "$fileName.$extensionNoDot")
        var duplicateNumber = 0
        while (file.exists()) {
            duplicateNumber++
            file = File(folder,  "$fileName ($duplicateNumber).$extensionNoDot")
        }
        return file
    }
}