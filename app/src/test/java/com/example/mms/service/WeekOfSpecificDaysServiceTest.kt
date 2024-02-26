package com.example.mms.service

import com.example.mms.model.HourWeight
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test


class WeekOfSpecificDaysServiceTest {


    @Test
    fun addInDayTest() {
        val service = WeekOfSpecificDaysService()

        service.addInDay(0, HourWeight(0, "12:00", 1))
        service.addInDay(0, HourWeight(0, "13:00", 3))

        assert(service.week[0].size == 2)
    }

    @Test
    fun addInDayOutOfRangeTest() {
        val service = WeekOfSpecificDaysService()

        service.addInDay(0, HourWeight(0, "12:00", 1))

        assertThrows(IndexOutOfBoundsException::class.java) {
            service.addInDay(7, HourWeight(0, "12:00", 1))
        }

        assertThrows(IndexOutOfBoundsException::class.java) {
            service.addInDay(-1, HourWeight(0, "12:00", 1))
        }

        assert(service.week[0].size == 1)
    }

    @Test
    fun setDayTest() {
        val service = WeekOfSpecificDaysService()

        service.addInDay(0, HourWeight(0, "12:00", 1))

        service.setDay(0, mutableListOf(HourWeight(0, "12:00", 1), HourWeight(0, "13:00", 3)))

        assert(service.week[0].size == 2)
    }

    @Test
    fun setDayOutOfRangeTest() {
        val service = WeekOfSpecificDaysService()

        assertThrows(IndexOutOfBoundsException::class.java) {
            service.setDay(7, mutableListOf(HourWeight(0, "12:00", 1), HourWeight(0, "13:00", 3)))
        }

        assert(service.week[0].size == 0)
    }

    @Test
    fun changeTimeTest() {
        val service = WeekOfSpecificDaysService()

        service.addInDay(0, HourWeight(0, "12:00", 1))

        service.changeTime(0, 0, "13:00")

        assert(service.week[0][0].hour == "13:00")
    }

    @Test
    fun changeTimeOutOfRangeTest() {
        val service = WeekOfSpecificDaysService()

        assertThrows(IndexOutOfBoundsException::class.java) {
            service.changeTime(0, 0, "13:00")
        }

        assert(service.week[0].size == 0)
    }

    @Test
    fun getDayTest() {
        val service = WeekOfSpecificDaysService()

        service.addInDay(0, HourWeight(0, "12:00", 1))
        service.addInDay(0, HourWeight(0, "13:00", 3))

        val day = service.getDay(0)

        assert(day.size == 2)
    }

    @Test
    fun getFlatTest() {
        val service = WeekOfSpecificDaysService()

        service.addInDay(0, HourWeight(0, "12:00", 1))
        service.addInDay(0, HourWeight(0, "13:00", 3))
        service.addInDay(1, HourWeight(0, "12:00", 1))
        service.addInDay(1, HourWeight(0, "13:00", 3))

        val flat = service.getFlat()

        assert(flat.size == 4)
    }
}
