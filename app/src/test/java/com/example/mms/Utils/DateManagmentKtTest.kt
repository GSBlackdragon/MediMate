package mms.example.Utils

import com.example.mms.Utils.areDatesOnSameDay
import com.example.mms.Utils.compareLocalDateTimeOnlyDays
import com.example.mms.Utils.dateToLocalDatetime
import com.example.mms.Utils.dateToString
import com.example.mms.Utils.extractMonthAndYearFromDate
import com.example.mms.Utils.getAgeFromStringBirthDate
import com.example.mms.Utils.getFormattedDate
import com.example.mms.Utils.hourMinuteToString
import com.example.mms.Utils.intToStringAdd0IfNecessary
import com.example.mms.Utils.stringHourMinuteToInt
import com.example.mms.Utils.stringToDate
import com.example.mms.Utils.stringddmmyyyyToDate
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Date


class DateManagmentKtTest {

    private fun nbMonthToString(nbMonth: Int): String {
        return when (nbMonth) {
            1 -> "janvier"
            2 -> "février"
            3 -> "mars"
            4 -> "avril"
            5 -> "mai"
            6 -> "juin"
            7 -> "juillet"
            8 -> "août"
            9 -> "septembre"
            10 -> "octobre"
            11 -> "novembre"
            12 -> "décembre"
            else -> ""
        }
    }

    @Test
    fun extractMonthAndYearFromDateFailTest() {
        val string = "ceci n'est pas une date"

        val monthYear = extractMonthAndYearFromDate(string)

        assert(monthYear == null)
    }

    @Test
    fun stringToDateTest() {
        val now = Date()
        val nowString = now.toString()

        val parseDate = stringToDate(nowString)

        assert(parseDate.toString() == now.toString())
    }

    @Test
    fun areDatesOnSameDayTest() {
        val now = Date()
        val hourInMs = 3600000
        val in1hour = Date(now.time + hourInMs)

        assert(areDatesOnSameDay(now, in1hour))
    }

    @Test
    fun getAgeFromStringBirthDateTest() {
        val calendar = Calendar.getInstance()
        calendar.set(2024, 1, 13)

        val age = getAgeFromStringBirthDate("01/01/2000", calendar)

        assert(age == 24)
    }

    @Test
    fun hourMinuteToStringTest() {
        val hour = 1
        val minute = 30
        val stringHourMinute = "01:30"

        val hourMinute = hourMinuteToString(hour, minute)

        assert(hourMinute == stringHourMinute)
    }

    @Test
    fun dateToStringTest() {
        val day = 1
        val month = 1
        val year = 2020
        val stringDate = "01/01/2020"

        val date = dateToString(day, month, year)

        assert(date == stringDate)
    }

    @Test
    fun stringddmmyyyyToDateTest() {
        val stringDate = "13/04/2020"
        val date = LocalDateTime.of(2020, 4, 13, 0, 0)

        val dateFromString = stringddmmyyyyToDate(stringDate)

        assert(dateFromString == date)
    }

    @Test
    fun intToStringAdd0IfNecessaryTest() {
        val number = 1
        val stringNumber = "01"

        val string = intToStringAdd0IfNecessary(number)

        assert(string == stringNumber)
    }

    @Test
    fun intToStringAdd0IfNecessary2Test() {
        val number = 10
        val stringNumber = "10"

        val string = intToStringAdd0IfNecessary(number)

        assert(string == stringNumber)
    }

    @Test
    fun stringHourMinuteToIntTest() {
        val stringHourMinute = "01:30"
        val hour = 1
        val minute = 30

        val hourMinute = stringHourMinuteToInt(stringHourMinute)

        assert(hourMinute.first == hour)
        assert(hourMinute.second == minute)
    }

    @Test
    fun dateToLocalDatetimeTest() {
        val date = Date()

        val localDateTimeFromDate = dateToLocalDatetime(date)

        val splittedDateString = date.toString().split(" ")
        val dayOfMonth = splittedDateString[2].toInt()

        assert(dayOfMonth == localDateTimeFromDate.dayOfMonth)
        assert(date.month + 1 == localDateTimeFromDate.monthValue)
        assert(date.year + 1900 == localDateTimeFromDate.year)
    }

    @Test
    fun compareLocalDateTimeOnlyDaysTest() {
        val date1 = LocalDateTime.of(2020, 1, 1, 0, 0)
        val date2 = LocalDateTime.of(2020, 1, 1, 1, 0)

        val dateAreInTheSameDay = compareLocalDateTimeOnlyDays(date1, date2)

        assert(dateAreInTheSameDay)
    }
}
