package com.example.mms.adapter

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mms.R
import com.example.mms.database.inApp.AppDatabase
import com.example.mms.model.HourWeight
import com.example.mms.model.MedicineStorage
import com.example.mms.model.Task
import com.example.mms.model.medicines.Medicine
import com.example.mms.service.TasksService

/**
 * Adapter for the recycler view of the tasks
 * @param context the context of the activity
 * @param items the list of tasks
 * @param db the database
 */
class TaskAdapter(
    private val context: Context,
    private val items : MutableList<Task>,
    private val db : AppDatabase
    )  : RecyclerView.Adapter<TaskAdapter.MyViewHolder>() {

    private val taskService = TasksService(context)

    /**
     * Class that represents the view holder of the recycler view
     * @param itemView the view
     */
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            val medicineName : TextView = itemView.findViewById(R.id.medicine_title)
            val medicineInformation : TextView = itemView.findViewById(R.id.medicine_information)
            val medicineFrequency : TextView = itemView.findViewById(R.id.medicine_my_medicines_frequency)
            val modifyTask : Button = itemView.findViewById(R.id.my_medicines_button_modify)
            val detailsTask : Button = itemView.findViewById(R.id.my_medicines_button_details)
    }

    /**
     * Function that creates the view holder
     * @param parent the parent view
     * @param viewType the view type
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_medicine_my_medicines, parent, false)
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
        var medicine : Medicine? = null
        // Get the medicine from the database in a Thread
        val t = Thread {
            medicine = db.medicineDao().getByCIS(item.medicineCIS)
            holder.medicineName.text = medicine!!.name
            holder.medicineInformation.text = medicine!!.type.weight
            holder.medicineFrequency.text = item.type
        }
        t.start()
        t.join()

        holder.modifyTask.setOnClickListener {
            dialogTask(item, medicine!!, true)
        }

        holder.detailsTask.setOnClickListener {
            dialogTask(item, medicine!!, false)
        }

    }

    /**
     * Function that creates the dialog for the task
     * @param task the task
     * @param medicine the medicine
     * @param edit if the task is editable
     */
    private fun dialogTask(task : Task, medicine: Medicine , edit : Boolean) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.custom_dialog_tasks)

        // Medicine
        val medicineName = dialog.findViewById<TextView>(R.id.tv_info_takes)
        medicineName.text = medicine.name
        val taskType = dialog.findViewById<TextView>(R.id.tv_info_takes2)
        taskType.text = task.type
        val dosageMedicine = dialog.findViewById<TextView>(R.id.dosage_medicine)
        dosageMedicine.text = medicine.type.generic
        val typeMedicine = dialog.findViewById<TextView>(R.id.type_medecine)
        typeMedicine.text = medicine.type.weight.toString()

        // Stock
        val tvStock = dialog.findViewById<TextView>(R.id.tv_stock)
        val stock = dialog.findViewById<TextView>(R.id.stock_value)

        // Get the stock or null for a medicine from the database in a Thread
        var medicineStorage : MedicineStorage? = null
        val thread = Thread {
            medicineStorage = db.medicineStorageDao().getMedicineStorageByMedicineId(medicine.code_cis)
        }
        thread.start()
        thread.join()
        if (medicineStorage != null) {
            tvStock.visibility = View.VISIBLE
            stock.text = medicineStorage!!.storage.toString()
        }else{
            tvStock.visibility = View.GONE
            stock.visibility = View.GONE
        }

        // Recycler view takes
        val rvTask = dialog.findViewById<RecyclerView>(R.id.rv_tasks)
        var list = mutableListOf<HourWeight>()
        val tt = Thread {
            list = this.taskService.getAllHourWeightFromTask(task)
        }
        tt.start()
        tt.join()

        val adapter = LittleTaskAdapter(context, list, db, edit, task.id) { dialog.dismiss() }
        rvTask.adapter = adapter
        rvTask.layoutManager = LinearLayoutManager(context)

        // Buttons
        val btnClose = dialog.findViewById<Button>(R.id.btn_cancel)
        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        val btnDelete = dialog.findViewById<Button>(R.id.btn_delete_task)
        if (edit) {
            btnDelete.visibility = View.VISIBLE
        } else {
            btnDelete.visibility = View.GONE
        }
        btnDelete.setOnClickListener {
            val t = Thread {
                db.taskDao().delete(task)
            }
            t.start()
            t.join()
            this.items.remove(task)
            notifyDataSetChanged()
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            if (list.isEmpty()) {
                this.items.remove(task)
                notifyDataSetChanged()
            }
        }

        dialog.show()
    }
}