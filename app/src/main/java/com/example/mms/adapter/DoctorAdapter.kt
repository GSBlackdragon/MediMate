package com.example.mms.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.mms.R
import com.example.mms.database.inApp.AppDatabase
import com.example.mms.model.Doctor

class DoctorAdapter(
    val doctorList : MutableList<Doctor>,
    val context : Context,
    val db : AppDatabase
    )  : RecyclerView.Adapter<DoctorAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomMedecin      = itemView.findViewById<TextView>(R.id.nomMedecin)
        val titleMedecin    = itemView.findViewById<TextView>(R.id.titleMedecin)
        val cityMedecin     = itemView.findViewById<TextView>(R.id.cityMedecin)
        val btnDelete       = itemView.findViewById<ImageView>(R.id.medecinDelete)
        val btnSms          = itemView.findViewById<TextView>(R.id.btn_sms)
        val btnMail         = itemView.findViewById<TextView>(R.id.btn_mail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_conseil_medecin, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return doctorList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item                    = doctorList[position]
        holder.nomMedecin.text      = item.firstname + " " + item.name
        holder.titleMedecin.text    = item.title
        holder.cityMedecin.text     = item.city
        holder.btnDelete.setOnClickListener {
            val tt = Thread {
                db.doctorDao().deleteDoctor(item)
            }
            tt.start()
            tt.join()
            doctorList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, doctorList.size)
            Toast.makeText(this.context, R.string.delete_medecin, Toast.LENGTH_SHORT).show()
        }
        if(item.email != "") {
            holder.btnMail.setOnClickListener {
                val mailAddress = item.email
                if (mailAddress.isNotEmpty()) {
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(mailAddress))
                    }
                    try {
                        this.context.startActivity(emailIntent)
                    } catch (ex: Exception) {
                        Toast.makeText(this.context, this.context.getString(R.string.erreur_lors_de_ouverture_mails), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this.context, this.context.getString(R.string.aucune_adresse_mail), Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            holder.btnMail.visibility = View.GONE
        }

        if (item.phoneNumber != "") {
            holder.btnSms.setOnClickListener {
                val phoneNumber = item.phoneNumber
                if (phoneNumber.isNotEmpty()) {
                    val smsUri = Uri.parse("smsto:$phoneNumber")
                    val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri)
                    try {
                        this.context.startActivity(smsIntent)
                    } catch (ex: Exception) {
                        Toast.makeText(this.context, this.context.getString(R.string.erreur_ouverture_sms), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this.context, this.context.getString(R.string.aucun_numero_telephone), Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            holder.btnSms.visibility = View.GONE
        }
    }

}