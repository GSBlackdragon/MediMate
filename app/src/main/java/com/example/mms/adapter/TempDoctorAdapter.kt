package com.example.mms.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mms.R
import com.example.mms.adapter.Interface.OnItemClickListener
import com.example.mms.model.Doctor

class TempDoctorAdapter(
    private val tempDoctors : MutableList<Doctor>,
    val context : Context)
    : RecyclerView.Adapter<TempDoctorAdapter.MyViewHolder>() {

    // interface for the click listener
    private var itemClickListener: OnItemClickListener? = null

    // function that sets the click listener
    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tv_doctor_name)
        val firstName: TextView = itemView.findViewById(R.id.tv_doctor_firstname)
        val title: TextView = itemView.findViewById(R.id.tv_doctor_title)
        val city: TextView = itemView.findViewById(R.id.tv_doctor_city)
        val isSelected: CheckBox = itemView.findViewById(R.id.checkbox_doctor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_choose_doctor, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return tempDoctors.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = tempDoctors[position]
        holder.name.text = item.name
        holder.firstName.text = item.firstname
        if (item.title.isEmpty()) {
            holder.title.text = item.speciality
        }else{
            holder.title.text = item.title
        }
        holder.city.text = item.city

        holder.isSelected.setOnCheckedChangeListener { _, _ ->
            itemClickListener?.onItemClick(position)
        }

    }
}