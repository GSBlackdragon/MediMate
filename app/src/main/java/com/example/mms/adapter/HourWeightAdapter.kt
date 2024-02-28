package com.example.mms.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mms.R
import com.example.mms.adapter.Interface.OnItemClickListener
import com.example.mms.model.HourWeight

/**
 * Adapter for the hour weight list
 *
 * @property context The context of the application
 * @property hourWeightList The list of hour weight
 */
class HourWeightAdapter(
    private val context: Context,
    var hourWeightList: MutableList<HourWeight>
) : RecyclerView.Adapter<HourWeightAdapter.MyViewHolder>() {

    private var itemClickListenerTP: OnItemClickListener? = null

    /**
     * Set the listener for the item click
     */
    fun setOnItemClickListener(listenerTimePicker: OnItemClickListener) {
        itemClickListenerTP = listenerTimePicker
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // get items view
        val tvHeure = itemView.findViewById<TextView>(R.id.text_timepicker)
        val npDosage = itemView.findViewById<NumberPicker>(R.id.np_dosage)
        val btnDelete = itemView.findViewById<ImageButton>(R.id.btn_delete)

        init {
            npDosage.minValue = 1
            npDosage.maxValue = 50
            npDosage.wrapSelectorWheel = true
        }
    }

    /**
     * Create the item view
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_heure_dosage, parent, false)
        return MyViewHolder(view)
    }

    /**
     * Get the number of items in the list
     */
    override fun getItemCount(): Int {
        return this.hourWeightList.size
    }

    /**
     * Bind data to the item view
     */
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // set text
        holder.tvHeure.text = this.hourWeightList[position].hour
        holder.npDosage.value = this.hourWeightList[position].weight

        // set listeners
        holder.npDosage.setOnValueChangedListener { _, _, newVal ->
            this.hourWeightList[position].weight = newVal
        }

        holder.tvHeure.setOnClickListener {
            this.itemClickListenerTP?.onItemClick(position)
        }

        holder.btnDelete.setOnClickListener {
            this.deleteItem(position)
        }
    }

    /**
     * Delete an item from the list and notify the adapter
     */
    private fun deleteItem(position: Int) {
        this.hourWeightList.removeAt(position)
        this.notifyItemRemoved(position)
        this.notifyItemRangeChanged(position, this.hourWeightList.size)
    }
}
