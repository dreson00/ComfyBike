package com.bk.bk1.utilities

import java.text.SimpleDateFormat
import java.util.Locale

fun parseFromDbFormat(time: String): String {
    val dbFormatter =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val date = dbFormatter.parse(time)
    val outputFormatter =
        SimpleDateFormat("d. MMMM yyyy HH:mm:ss", Locale.forLanguageTag("cs"))
    if (date == null) {
        return String()
    }
    return outputFormatter.format(date)
}