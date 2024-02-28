package com.example.mms.adapter.Interface

fun interface OnItemClickListener {
    fun onItemClick(position: Int)
}

fun interface CalendarAdapterInterface {
    fun onMonthYearChanged(month: String, year: String)
}
