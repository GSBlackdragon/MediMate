package com.example.mms.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mms.R
import com.example.mms.model.ShowableTakes
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Adapter for the recycler view of the tasks
 *
 * @param context the context of the activity
 * @param items the list of tasks
 */
class TakesHistoryAdapter (
    private val context: Context,
    private val items: MutableList<ShowableTakes>
): RecyclerView.Adapter<TakesHistoryAdapter.MyViewHolder>() {

    /**
     * Class that represents the view holder of the recycler view
     * @param itemView the view
     */
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medicineName: TextView = itemView.findViewById(R.id.medicine_name)
        val medicineDoseForme: TextView = itemView.findViewById(R.id.medicine_dose_forme)
        val takeAtDate: TextView = itemView.findViewById(R.id.heure_prise)
        val takeAtHour: TextView = itemView.findViewById(R.id.date_prise)
    }

    /**
     * Function that creates the view holder
     * @param parent the parent view
     * @param viewType the view type
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_takes_history, parent, false)
        return MyViewHolder(view)
    }

    /**
     * Function that returns the number of items
     */
    override fun getItemCount(): Int {
        return this.items.size
    }

    /**
     * Function that binds the view holder
     * @param holder the view holder
     * @param position the position of the item
     */
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = this.items[position]

        holder.medicineName.text = item.medicineName
        holder.medicineDoseForme.text = this.context.getString(R.string.doses_weight_type, item.weight.toString(), item.medicineType)

        val hourFormatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault())
        val hourString = item.takeAt.format(hourFormatter)

        holder.takeAtHour.text = hourString

        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
        val dateString = item.takeAt.format(dateFormatter)

        holder.takeAtDate.text = dateString

    }
}
