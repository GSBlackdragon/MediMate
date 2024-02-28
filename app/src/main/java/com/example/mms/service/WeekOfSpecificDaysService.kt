package com.example.mms.service

import com.example.mms.model.HourWeight

/**
 * Service to manage the week of specific days
 * It contains a list of days, each day contains a list of hours with their weight
 * day 0 = Monday ... day 6 = Sunday
 *
 * @property week the week of specific days
 */
class WeekOfSpecificDaysService {

    val week = MutableList<MutableList<HourWeight>>(7) { mutableListOf() }

    /**
     * Add an hour with its weight in a day
     */
    fun addInDay(day: Int, hourWeight: HourWeight) {
        this.week[day].add(hourWeight)
    }

    /**
     * Set the hours with their weight in a day
     */
    fun setDay(day: Int, hoursWeights: List<HourWeight>) {
        this.week[day] = hoursWeights.toMutableList()
    }

    /**
     * Change the weight of an hour in a day
     */
    fun changeTime(day: Int, position: Int, newTime: String) {
        this.week[day][position].hour = newTime
    }

    /**
     * Get all the hours with their weight in a day
     *
     * @return the list of hours with their weight
     */
    fun getDay(day: Int): MutableList<HourWeight> {
        return this.week[day]
    }

    /**
     * Get all the hours with their weight in the week in one list
     *
     * @return the list of hours with their weight
     */
    fun getFlat(): MutableList<HourWeight> {
        val flat = mutableListOf<HourWeight>()
        for (day in this.week) {
            flat.addAll(day)
        }
        return flat
    }
}
