package software.setixx.chimu.presentation.utils

import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

object DateTimeUtils {
    fun formatDateTime(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""
        return try {
            val instant = Instant.parse(isoString)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            
            val day = localDateTime.day.toString().padStart(2, '0')
            val month = localDateTime.month.number.toString().padStart(2, '0')
            val year = localDateTime.year
            val hour = localDateTime.hour.toString().padStart(2, '0')
            val minute = localDateTime.minute.toString().padStart(2, '0')
            
            "$day.$month.$year $hour:$minute"
        } catch (e: Exception) {
            isoString
        }
    }

    fun formatDate(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""
        return try {
            val instant = Instant.parse(isoString)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            
            val day = localDateTime.day.toString().padStart(2, '0')
            val month = localDateTime.month.number.toString().padStart(2, '0')
            val year = localDateTime.year
            
            "$day.$month.$year"
        } catch (e: Exception) {
            isoString
        }
    }
}
