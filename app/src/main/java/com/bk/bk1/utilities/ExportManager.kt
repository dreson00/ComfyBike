package com.bk.bk1.utilities

import android.graphics.Bitmap
import android.os.Environment
import com.bk.bk1.enums.ExportCsvStatus
import com.bk.bk1.models.ComfortIndexRecord
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ExportManager {
    fun saveBitmapAsPng(bitmap: Bitmap, fileName: String): Int {
        val folder = getOrCreateFolder()
        val file = createFile(folder, fileName, "png")

        return try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

            outputStream.flush()
            outputStream.close()
            0
        }
        catch (e: Exception) {
            1
        }
    }

    fun saveCiListAsCsv(records: List<ComfortIndexRecord>, fileName: String): ExportCsvStatus {
        val folder = getOrCreateFolder()
        val file = createFile(folder, fileName, "csv")

        return try {
            file.bufferedWriter().use { out ->
                out.write("comfortIndex,speed,latitude,longitude\n")
                records.forEach { record ->
                    out.write("${record.comfortIndex},${record.bicycleSpeed},${record.latitude},${record.longitude}\n")
                }
            }
            ExportCsvStatus.SUCCESS
        } catch (e: IOException) {
            ExportCsvStatus.FAILURE
        }
    }

    private fun getOrCreateFolder(): File {
        val externalDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val appDirectory = File(externalDirectory, "ComfyBike")

        if (!appDirectory.exists()) {
            appDirectory.mkdirs()
        }
        return appDirectory
    }

    // Creates a file with a unique name.
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