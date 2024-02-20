package com.example.mms.adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mms.R
import com.example.mms.Utils.dateToLocalDatetime
import com.example.mms.Utils.extractMonthAndYearFromDate
import com.example.mms.Utils.stringToDate
import com.example.mms.adapter.Interface.CalendarAdapterInterface
import com.example.mms.adapter.Interface.OnItemClickListener
import com.example.mms.model.CalendarDay
import java.time.format.TextStyle
import java.util.Locale


/**
 * Adapter for the recycler view of the calendar
 *
 * @param context the context of the activity
 * @param days the list of days
 * @param itemWith the width of the item
 */
class CalendarAdapter(
    private val context: Context,
    private val days: List<CalendarDay>,
    private val itemWith: Int
) :
    RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

    // interface for the click listener
    private var itemClickListener: OnItemClickListener? = null
    // interface for the calendar adapter
    private var calendarAdapterInterface: CalendarAdapterInterface? = null

    // function that sets the click listener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    // function that sets the calendar adapter
    fun setCalendarAdapterInterface(listener: CalendarAdapterInterface) {
        calendarAdapterInterface = listener
    }

    // class that represents the view holder of the recycler view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayNumberView: TextView = itemView.findViewById(R.id.calendarDay_number)
        val dayTextView: TextView = itemView.findViewById(R.id.calendarDay_day)
        val points: LinearLayout = itemView.findViewById(R.id.calendarDay_point)
    }

    // function that creates the view holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_calendar, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // set item width
        val layoutParams = holder.itemView.layoutParams
        layoutParams.width = itemWith

        val day = days[position]
        val dayNumberView = holder.dayNumberView
        dayNumberView.text = day.dayOfMonth.toString()
        val dayText = dateToLocalDatetime(stringToDate(day.date)).dayOfWeek.getDisplayName(
            TextStyle.SHORT,
            Locale.getDefault()
        )
        // set day text depends of locale language (fr -> lun,mard..) (en -> mon,tue..)
        holder.dayTextView.text = dayText

        // set day text and background color depends of day
        if (day.isSelected) {
            dayNumberView.setTextColor(ContextCompat.getColor(context, R.color.white))
            dayNumberView.setBackgroundResource(R.drawable.selected_day_background)
        } else {
            dayNumberView.setTextColor(ContextCompat.getColor(context, R.color.black))
            dayNumberView.setBackgroundResource(0)
        }

        // set the on click listener
        holder.itemView.setOnClickListener {
            itemClickListener?.onItemClick(position)
            calendarAdapterInterface?.onMonthYearChanged(
                extractMonthAndYearFromDate(day.date)?.first.orEmpty(),
                extractMonthAndYearFromDate(day.date)?.second.orEmpty()
            )
        }
        // set the points of the day (number of tasks)
        holder.points.removeAllViews()
        holder.points.addView(createLinearLayoutWithImage(day.listTasks.size))
    }

    // function that returns the number of items
    override fun getItemCount(): Int {
        return days.size
    }

    // function that toggles the day selection
    fun toggleDaySelection(position: Int) {
        days.forEach { day -> day.isSelected = false }
        val day = days[position]
        day.isSelected = true
        notifyDataSetChanged()
    }


    // function that creates the linear layout with the points
    fun createLinearLayoutWithImage(nb: Int): LinearLayout {
        // max 3 points per day (too big if more)
        var numberOfImage = nb
        if (numberOfImage > 3) {
            numberOfImage = 3
        }
        // we create a linear layout with horizontal orientation
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        // we center the linear layout
        linearLayout.gravity = Gravity.CENTER

        // we get the width of the screen
        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels

        val marginPercentage = 0.022
        val margin = (width * marginPercentage).toInt()

        // we add the points to the linear layout
        // the points are represented by image view
        // the color depends of the index
        for (i in 0 until numberOfImage) {
            val imageView = ImageView(context)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(-margin, 0, -margin, 0)
            imageView.layoutParams = layoutParams
            imageView.setImageResource(getDrawableImageForIndex(i))
            linearLayout.addView(imageView)
        }

        return linearLayout
    }

    // function that returns the drawable image for the index
    fun getDrawableImageForIndex(index: Int): Int {
        return when (index) {
            0 -> R.drawable.blue_point
            1 -> R.drawable.purple_point
            2 -> R.drawable.green_point
            else -> R.drawable.blue_point
        }
    }
}