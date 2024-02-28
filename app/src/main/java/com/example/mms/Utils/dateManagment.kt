package com.example.mms.Utils

import android.content.Context
import com.example.mms.database.inApp.SingletonDatabase
import com.example.mms.model.CalendarDay
import com.example.mms.model.Task
import com.example.mms.service.TasksService
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Extract month and year from a date string
 * @param dateString date string
 * @return Pair of month and year
 */
fun extractMonthAndYearFromDate(dateString: String): Pair<String, String>? {
    try {
        val inputFormat = SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val date = inputFormat.parse(dateString)

        val localDateTime = dateToLocalDatetime(date!!)
        val month = localDateTime.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val year = localDateTime.year.toString()

        return Pair(month, year)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}


/**
 * Converts a string to a date, using the format "E MMM dd HH:mm:ss zzz yyyy"
 * @param dateString date string
 * @return Date
 */
fun stringToDate(dateString: String): Date {
    val inputFormat = SimpleDateFormat("E MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
    return inputFormat.parse(dateString)!!
}

/**
 * Check if two dates are on the same day, ignoring the time
 */
fun areDatesOnSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }

    return cal1[Calendar.YEAR] == cal2[Calendar.YEAR] &&
            cal1[Calendar.MONTH] == cal2[Calendar.MONTH] &&
            cal1[Calendar.DAY_OF_MONTH] == cal2[Calendar.DAY_OF_MONTH]
}

/**
 * get the current date as displayable string
 *
 * @return the current date as displayable string
 */
fun getFormattedDate(dateTime: LocalDateTime): String {
    val date = dateTime.toLocalDate()
    val time = dateTime.toLocalTime()

    // set the date
    val dayOfWeek = date.dayOfWeek
    val dayOfMonth = date.dayOfMonth
    val month = date.month

    // set the time
    val hour = time.hour
    val minute = time.minute

    return String.format(
        "%s %d %s %d:%02d",
        dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
        dayOfMonth,
        month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
        hour,
        minute
    )
}

fun getNewCalendarDayList(
    previousCalendarDays: List<CalendarDay>,
    daySelected: Date,
    context: Context
): MutableList<CalendarDay> {
    val calendarDays = mutableListOf<CalendarDay>()

    val calendar = Calendar.getInstance()
    calendar.time = daySelected
    calendar.add(Calendar.DAY_OF_MONTH, -1) // Jour précédent


    val db = SingletonDatabase.getDatabase(context)
    val tasksService = TasksService(context)

    var email = ""
    var t = Thread {
        email = db.userDao().getConnectedUser()!!.email
    }
    t.start()
    t.join()


    if (previousCalendarDays.isEmpty()) {
        // Add new random CalendarDays for the previous days
        for (i in 0 until 5) {
            val date = calendar.time
            val dayOfMonth = calendar[Calendar.DAY_OF_MONTH]
            val isSelected = (daySelected == date)

            var listOfTask = mutableListOf<Task>()
            t = Thread {
                listOfTask = tasksService.getTasksAt(email, date)
            }
            t.start()
            t.join()


            val calendarDay = CalendarDay(date.toString(), dayOfMonth, listOfTask, isSelected)
            calendarDays.add(calendarDay)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return calendarDays
    }

    // Generate all new date and check in previous list if it exists, if yes use them instead of random
    val allNewDates = mutableListOf<Date>()
    for (i in 0 until 5) {
        val date = calendar.time
        allNewDates.add(date)
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }

    // Check if date exists in previous list
    calendar.add(Calendar.DAY_OF_MONTH, -5)
    for (date in allNewDates) {
        val calendarDay = previousCalendarDays.find { it.date == date.toString() }
        if (calendarDay != null) {
            calendarDays.add(calendarDay)
        } else {
            val dayOfMonth = calendar[Calendar.DAY_OF_MONTH]
            val isSelected = (daySelected == date)
            var listOfTask = mutableListOf<Task>()
            t = Thread {
                listOfTask = tasksService.getTasksAt(email, date)
            }
            t.start()
            t.join()
            val newCalendarDay = CalendarDay(date.toString(), dayOfMonth, listOfTask, isSelected)
            calendarDays.add(newCalendarDay)
        }
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }

    return calendarDays
}

/**
 * Calculate the age from a birth date
 *
 * @param birthDate the birth date
 * @param calendar the calendar to use, use in tests
 *
 * @return the age
 */
fun getAgeFromStringBirthDate(birthDate: String, calendar: Calendar = Calendar.getInstance()): Int {
    val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRENCH)
    val date = inputFormat.parse(birthDate)
    calendar.time = date!!
    val year = calendar[Calendar.YEAR]
    val today = Calendar.getInstance()
    var age = today[Calendar.YEAR] - year
    if (today[Calendar.DAY_OF_YEAR] < calendar[Calendar.DAY_OF_YEAR]) {
        age--
    }
    if (age < 0) {
        age = 0
    }
    return age
}

/**
 * Create a string with the hour and the minute
 * Add a 0 if necessary
 *
 * @param hour the hour
 * @param minute the minute
 *
 * @return the string
 */
fun hourMinuteToString(hour: Int, minute: Int): String {
    return "${intToStringAdd0IfNecessary(hour)}:${intToStringAdd0IfNecessary(minute)}"
}

/**
 * Create a string with the day, the month and the year
 * Add a 0 if necessary
 *
 * @param day the day
 * @param month the month
 * @param year the year
 *
 * @return the string
 */
fun dateToString(day: Int, month: Int, year: Int): String {
    return "${intToStringAdd0IfNecessary(day)}/${intToStringAdd0IfNecessary(month)}/$year"
}

/**
 * Transform a string dd/mm/yyyy to a LocalDateTime
 *
 * @param string the string format dd/mm/yyyy
 *
 * @return the LocalDateTime
 */
fun stringddmmyyyyToDate(string: String): LocalDateTime {
    val splited = string.split("/")
    val day = splited[0].toInt()
    val month = splited[1].toInt()
    val year = splited[2].toInt()
    return LocalDateTime.of(year, month, day, 0, 0)
}

/**
 * Int to string with a 0 if necessary
 */
fun intToStringAdd0IfNecessary(number: Int): String {
    return if (number < 10) {
        "0$number"
    } else {
        number.toString()
    }
}

/**
 * Transform a string hh:mm to a Pair of Int
 *
 * @param hourMinute the string format hh:mm
 *
 * @return the Pair of Int
 */
fun stringHourMinuteToInt(hourMinute: String): Pair<Int, Int> {
    val splited = hourMinute.split(":")
    val hour = splited[0].toInt()
    val minute = splited[1].toInt()

    return Pair(hour, minute)
}

/**
 * Convert a date to a LocalDateTime
 *
 * @param dateToConvert the date to convert
 *
 * @return the LocalDateTime
 */
fun dateToLocalDatetime(dateToConvert: Date): LocalDateTime {
    return LocalDateTime.ofInstant(dateToConvert.toInstant(), ZoneId.systemDefault())
}

/**
 * Compare two LocalDateTime only on days
 *
 * @param date1 the first date
 * @param date2 the second date
 *
 * @return true if the two dates are on the same day, false otherwise
 */
fun compareLocalDateTimeOnlyDays(date1: LocalDateTime, date2: LocalDateTime): Boolean {
    return date1.toLocalDate() == date2.toLocalDate()
}
