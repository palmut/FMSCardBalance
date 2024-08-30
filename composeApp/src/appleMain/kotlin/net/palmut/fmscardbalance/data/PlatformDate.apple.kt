import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterStyle

actual fun getDate(): String {
    val dateFormatter = NSDateFormatter()
    dateFormatter.dateStyle = NSDateFormatterStyle.MediumStyle
    dateFormatter.timeStyle = NSDateFormatterStyle.NoStyle
    dateFormatter.dateFormat = "dd MMM"
    return dateFormatter.stringFromDate(NSDate())
}