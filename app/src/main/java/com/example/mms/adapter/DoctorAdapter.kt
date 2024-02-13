package com.example.mms.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mms.R
import com.example.mms.model.Doctor

class DoctorAdapter(val doctorList : MutableList<Doctor>, val context : Context)  : RecyclerView.Adapter<DoctorAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomMedecin = itemView.findViewById<TextView>(R.id.nomMedecin)
        val numMedecin = itemView.findViewById<TextView>(R.id.numeroMedecin)
        val emailMedecin = itemView.findViewById<TextView>(R.id.mailMedecin)
        val btnSms = itemView.findViewById<TextView>(R.id.btn_sms)
        val btnMail = itemView.findViewById<TextView>(R.id.btn_mail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_heure_dosage, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return doctorList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = doctorList[position]
        holder.nomMedecin.text = item.firstname + " " + item.name
        holder.numMedecin.text = item.rpps.toString()
        holder.emailMedecin.text = item.email
    }

}