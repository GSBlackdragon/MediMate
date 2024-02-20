package com.example.mms.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.mms.R
import com.example.mms.database.inApp.AppDatabase
import com.example.mms.model.HourWeight

/**
 * Adapter for the hour weight list
 *
 * @property context The context of the application
 * @property items The list of hour weight
 * @property db The database
 * @property edit If the user is editing the task
 * @property taskId The id of the task
 * @property dismiss The function to dismiss the dialog
 */
class LittleTaskAdapter(
    private val context: Context,
    private val items : MutableList<HourWeight>,
    private val db : AppDatabase,
    private val edit : Boolean,
    private val taskId : Long,
    private val dismiss : () -> Unit
) : RecyclerView.Adapter<LittleTaskAdapter.MyViewHolder>() {


    // class that represents the view holder of the recycler view
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val hour : TextView = itemView.findViewById(R.id.tv_heure)
        val weight : TextView = itemView.findViewById(R.id.tv_nb_doses)
        val btnDelete : Button = itemView.findViewById(R.id.btn_delete)
    }

    // function that creates the view holder
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_edit_task, parent, false)
        return MyViewHolder(view)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = this.items[position]
        holder.hour.text = item.hour + "h"
        holder.weight.text = item.weight.toString() + context.getString(R.string.doses)
        // if the user is editing the task, we show the delete button
        if (edit) {
            holder.btnDelete.visibility = View.VISIBLE
        } else {
            holder.btnDelete.visibility = View.GONE
        }
        // if the user click on the delete button, we delete the hour weight
        holder.btnDelete.setOnClickListener {
            val tt = Thread {
                db.hourWeightDao().delete(item)
                if (items.size == 1) {
                    db.taskDao().deleteById(taskId)
                }
            }
            tt.start()
            tt.join()
            // if the user delete the last hour weight, we dismiss the dialog
            if (items.size == 1) {
                dismiss()
            }
            Toast.makeText(context, R.string.deleted, Toast.LENGTH_SHORT).show()
            notifyItemRemoved(position)
            this.items.remove(item)
        }
    }
    // function that returns the number of items
    override fun getItemCount(): Int {
        return this.items.size
    }
}