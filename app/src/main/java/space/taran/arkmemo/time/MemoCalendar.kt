package space.taran.arkmemo.time

import java.util.*

class MemoCalendar {
    companion object{

        private fun getCurrentTime(): Calendar{
            return Calendar.getInstance()
        }

        private fun formatFullDate() = with(getCurrentTime()) {
            val month = get(Calendar.MONTH) + 1
            "${Week.getDayOfWeekName(get(Calendar.DAY_OF_WEEK))} " +
                    "${get(Calendar.DATE)}/" +
                    "$month/" +
                    "${get(Calendar.YEAR)} " +
                    "${get(Calendar.HOUR_OF_DAY)}:${get(Calendar.MINUTE)}:${get(Calendar.SECOND)}"
        }

        private fun formatDate() =  with(getCurrentTime()) {
            val month = get(Calendar.MONTH) + 1
            "${Week.getDayOfWeekName(get(Calendar.DAY_OF_WEEK))} " +
                    "${get(Calendar.DATE)}/" +
                    "$month/" +
                    "${get(Calendar.YEAR)}"
        }

        private fun formatTime() = with(getCurrentTime()){
            "${get(Calendar.HOUR_OF_DAY)}:${get(Calendar.MINUTE)}:${get(Calendar.SECOND)}"
        }

        fun getFullDateToday() = formatFullDate()

        fun getDateToday() = formatDate()

        fun getTimeNow() = formatTime()
    }

    private object Week{
        private const val SUNDAY = "Sunday"
        private const val MONDAY = "Monday"
        private const val TUESDAY = "Tuesday"
        private const val WEDNESDAY = "Wednesday"
        private const val THURSDAY = "Thursday"
        private const val FRIDAY = "Friday"
        private const val SATURDAY = "Saturday"

        fun getDayOfWeekName(dayOfWeek: Int):String?{
            return when(dayOfWeek) {
                Calendar.SUNDAY -> SUNDAY
                Calendar.MONDAY -> MONDAY
                Calendar.TUESDAY -> TUESDAY
                Calendar.WEDNESDAY -> WEDNESDAY
                Calendar.THURSDAY -> THURSDAY
                Calendar.FRIDAY -> FRIDAY
                Calendar.SATURDAY -> SATURDAY
                else -> null
            }
        }
    }
}