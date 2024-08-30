package net.palmut.fmscardbalance.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun getDate(): String {
    val date = Date()
    val simpleDateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    return simpleDateFormat.format(date)
}