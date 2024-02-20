package com.example.mms.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mms.R
import com.example.mms.adapter.Interface.OnItemClickListener

/**
 * Adapter for the search bar
 *
 * @property context The context of the application
 * @property medicinesNames The list of medicines names
 */
class MediProposalsAdapter(
    private val context: Context,
    val medicinesNames: List<String>
) :
    RecyclerView.Adapter<MediProposalsAdapter.MyViewHolder>() {

    private var itemClickListener: OnItemClickListener? = null

    /**
     * Set the listener for the item click
     */
    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // get item view
        val medicineName = itemView.findViewById<TextView>(R.id.proposalName)
    }

    /**
     * Create the item view
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_proposal_medicine, parent, false)
        return MyViewHolder(view)
    }

    /**
     * Bind data to the item view
     */
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // get the medicine name
        val medicineName = medicinesNames[position]

        // set the medicine name
        holder.medicineName.text = medicineName

        // set the click listener
        holder.itemView.setOnClickListener {
            itemClickListener?.onItemClick(position)
        }
    }

    /**
     * Get the number of items in the list
     */
    override fun getItemCount(): Int {
        return this.medicinesNames.size
    }
}
