package net.palmut.fmscardbalance.data

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterNoStyle

actual fun getDate(): String {
    val dateFormatter = NSDateFormatter()
    dateFormatter.dateStyle = NSDateFormatterMediumStyle
    dateFormatter.timeStyle = NSDateFormatterNoStyle
    dateFormatter.dateFormat = "dd MMM"
    return dateFormatter.stringFromDate(NSDate())
}