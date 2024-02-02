package com.example.mms.adapter

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.mms.R
import com.example.mms.Utils.areDatesOnSameDay
import com.example.mms.Utils.getFormattedDate
import com.example.mms.database.inApp.AppDatabase
import com.example.mms.model.HourWeight
import com.example.mms.model.ShowableHourWeight
import com.example.mms.model.Takes
import com.example.mms.model.Task
import com.example.mms.service.MedicineStorageService
import com.example.mms.service.TasksService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDateTime
import java.util.Date

/**
 * Adapter for the recycler view of the tasks
 * @param context the context of the activity
 * @param items the list of tasks
 * @param db the database
 * @param currentDate the current date
 * @param view the view
 * @param funUpdateSmiley the function that updates the smiley
 */
class TakesAdapter(
    private val context: Context,
    private val items: MutableList<ShowableHourWeight>,
    private val db : AppDatabase,
    private val currentDate : Date,
    private val view : View,
    private var listSubActiveDoublons : MutableList<Int?>,
    private val funUpdateSmiley : () -> Unit

) :
    RecyclerView.Adapter<TakesAdapter.MyViewHolder>() {

    // Services
    private val tasksService = TasksService(context)
    private val mStorageService = MedicineStorageService(context,view)


    /**
     * Class that represents the view holder of the recycler view
     * @param itemView the view
     */
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskTitle: TextView = itemView.findViewById(R.id.medicine_title)
        val medicineImage  : ImageView = itemView.findViewById(R.id.medicine_image)
        val medicineInformation : TextView = itemView.findViewById(R.id.medicine_information)
        val taskTime : TextView = itemView.findViewById(R.id.medicine_home_time)
        val timeRemaining : TextView = itemView.findViewById(R.id.medicine_home_time_remaining)
        val buttonTaskChecked : FloatingActionButton = itemView.findViewById(R.id.medicine_home_check)
        val tvStock : TextView = itemView.findViewById(R.id.tv_stock)
        val stock : TextView = itemView.findViewById(R.id.stock_value)
        val alertImage : ImageView = itemView.findViewById(R.id.alertSameSubstance)
    }

    /**
     * Function that creates the view holder
     * @param parent the parent view
     * @param viewType the view type
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_medicine_home, parent, false)
        return  MyViewHolder(view)
    }


    /**
     * Function that binds the view holder
     * @param holder the view holder
     * @param position the position of the item
     */
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = this.items[position]
        context.run {
            holder.taskTitle.text = item.medicineName
        }

        var itemSubActCode : Int? = null
        var t = Thread {
            itemSubActCode = db.medicineDao().getByCIS(item.task.medicineCIS)?.composition?.substance_code
        }
        t.start()
        t.join()


        if (itemSubActCode != null && itemSubActCode in listSubActiveDoublons){
            holder.alertImage.visibility=View.VISIBLE
        }else{
            holder.alertImage.visibility=View.GONE
        }

        holder.alertImage.setOnLongClickListener {
            Toast.makeText(context, R.string.toast_sub_active, Toast.LENGTH_LONG).show()
            true
        }
        holder.medicineInformation.text = item.task.type

        holder.stock.visibility = View.GONE
        holder.tvStock.visibility = View.GONE
        // if the medicine is in the storage we display the stock
        if (item.medicineStorage != null) {
            holder.tvStock.visibility = View.VISIBLE
            holder.stock.visibility = View.VISIBLE
            holder.stock.text = item.medicineStorage.storage.toString()
            holder.stock.setTextColor(context.getColor(R.color.black))
            if (item.medicineStorage.storage <= item.medicineStorage.alertValue) {
                holder.stock.setTextColor(context.getColor(R.color.red))
            }
        }
        holder.taskTime.text = this.context.getString(R.string.a_heures, item.hourWeight.hour)


        // Get the remaining time
        val hourSplited = getHoursMinutesRemaining(item.hourWeight)
        val hoursRemaining = hourSplited.first
        val minutesRemaining = hourSplited.second

        // Display the remaining time
        holder.timeRemaining.text = if (hoursRemaining < 0) {
            context.getString(R.string.dans) + (hoursRemaining * -1).toString() + " " + if (hoursRemaining == -1) context.getString(R.string.heure) else context.getString(R.string.heures)
        } else if (hoursRemaining == 0) {
            if (minutesRemaining < 0) {
                context.getString(R.string.dans) + (minutesRemaining * -1).toString() + " " + if (minutesRemaining == -1) context.getString(R.string.minute) else context.getString(R.string.minutes)
            } else {
                if (minutesRemaining == 1) {
                    context.getString(R.string.il_y_a_minute)
                } else {
                    context.getString(R.string.il_y_a_minutes, minutesRemaining.toString())
                }
            }
        } else {
            if (hoursRemaining == 1) {
                context.getString(R.string.il_y_a_heure)
            } else {
                context.getString(R.string.il_y_a_heures, hoursRemaining.toString())
            }
        }

        // If the task is done we change the color of the item
        holder.itemView.setBackgroundColor(context.getColor(R.color.white))
        holder.buttonTaskChecked.setImageResource(R.drawable.baseline_check_24)
        val today = Date()
        if (areDatesOnSameDay(today, currentDate)) {
            // Know if the task is done or not (HourWeight isDone)
            var takes: Takes? = null
            val t = Thread {
                takes = this.tasksService.getOrCreateTakes(item.hourWeight.id, LocalDateTime.now().toLocalDate().atStartOfDay())
            }
            t.start()
            t.join()
                if (takes != null) {
                    // If the task is done we change the color of the item
                    if (takes!!.isDone) {
                        context.run {
                            holder.buttonTaskChecked.setImageResource(R.drawable.baseline_info_24)
                        }
                        holder.buttonTaskChecked.setOnClickListener {
                            dialogDetails(item, true, position)
                        }
                    } else {
                        // Else we set on click of the button buttonTaskChecked the action of validate the takes
                        holder.buttonTaskChecked.setOnClickListener {
                            val tt = Thread {
                                var dismiss = false
                                if (item.medicineStorage != null) {
                                    dismiss = this.mStorageService.updateMedicineStorage(
                                        item.medicineStorage,
                                        item.hourWeight.weight,
                                        item.medicineName,
                                        this@TakesAdapter
                                    )
                                }
                                if(!dismiss) {
                                    db.takesDao().updateIsDone(true, item.hourWeight.id, LocalDateTime.now().toLocalDate().atStartOfDay(), LocalDateTime.now())
                                    funUpdateSmiley()
                                }
                            }
                            tt.start()
                            tt.join()
                            this@TakesAdapter.notifyDataSetChanged()
                        }
                    }
                }
            } else {
                // if the takes is not for today we change the color of the item
                holder.itemView.setBackgroundColor(context.getColor(R.color.light_gray))
                holder.buttonTaskChecked.setImageResource(R.drawable.baseline_info_24)
                holder.buttonTaskChecked.setOnClickListener {
                    dialogDetails(item, false, position)
                }
            }
         //TODO get image by medicine type
    }

    override fun getItemCount(): Int {
        return this.items.size
    }

    fun updateCurrentDate(newDate : Date) {
        this.currentDate.time = newDate.time
        this.notifyDataSetChanged()
    }

    fun updateListDoublons(list : MutableList<Int?>) {
        this.listSubActiveDoublons = list
        this.notifyDataSetChanged()
    }

    fun getImageByMedicineType(task: Task) {
         //TODO
    }

    /**
     * Function that returns the remaining time
     * @param hW the hour weight
     * @return the remaining time
     */
    fun getHoursMinutesRemaining(hW: HourWeight): Pair<Int, Int> {
        val hour = hW.hour.split(":")[0].toInt()
        return Pair(
            LocalDateTime.now().hour - hour,
            LocalDateTime.now().minute - hW.hour.split(":")[1].toInt()
        )
    }

    /**
     * Function that creates the dialog for the takes
     *
     * @param sHw the showable hour weight
     * @param taked if the takes is taked
     * @param position the position of the item
     */
    private fun dialogDetails(sHw: ShowableHourWeight, taked : Boolean = false, position: Int) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.custom_dialog_info_takes)

        // Medicine
        val medicineName = dialog.findViewById<TextView>(R.id.tv_info_takes)
        medicineName.text = sHw.medicineName
        val taskType = dialog.findViewById<TextView>(R.id.tv_info_takes2)
        taskType.text = sHw.task.type
        val dosageMedicine = dialog.findViewById<TextView>(R.id.dosage_medicine)
        dosageMedicine.text = sHw.medicineType.generic
        val typeMedicine = dialog.findViewById<TextView>(R.id.type_medecine)
        typeMedicine.text = sHw.medicineType.weight.toString()

        // Dates
        val creationDate = dialog.findViewById<TextView>(R.id.creation_date)
        creationDate.text = getFormattedDate( sHw.task.createdAt)
        val updateDate = dialog.findViewById<TextView>(R.id.update_date)
        updateDate.text = getFormattedDate(sHw.task.updatedAt)

        // Stock
        val tvStock = dialog.findViewById<TextView>(R.id.tv_stock)
        val stock = dialog.findViewById<TextView>(R.id.stock_value)
        if (sHw.medicineStorage != null) {
            tvStock.visibility = View.VISIBLE
            stock.text = sHw.medicineStorage.storage.toString()
            tvStock.setOnClickListener {
                dialog.dismiss()
                this.mStorageService.dialogGererStock(sHw.medicineStorage, sHw.medicineName, this)
            }
        }else{
            tvStock.visibility = View.GONE
            stock.visibility = View.GONE
        }

        // Buttons
        val btnCancelTakes = dialog.findViewById<TextView>(R.id.btn_avoid_take)
        // if the takes is taked we display the button to cancel the takes
        if (taked) {
            btnCancelTakes.text = context.getString(R.string.annuler_la_prise)
            btnCancelTakes.setOnClickListener {
                val tt = Thread {
                    // We update the takes
                    db.takesDao().updateIsDone(false, sHw.hourWeight.id, LocalDateTime.now().toLocalDate().atStartOfDay(), LocalDateTime.now())
                    // We update the smiley
                    funUpdateSmiley()
                    // We update the stock
                    if (sHw.medicineStorage != null) {
                        sHw.medicineStorage.storage += sHw.hourWeight.weight
                        db.medicineStorageDao().update(sHw.medicineStorage)
                    }
                }
                tt.start()
                tt.join()
                // We update the recycler view
                this@TakesAdapter.notifyItemChanged(position)
                dialog.dismiss()
            }
        }else{
            // else we hide the button
            btnCancelTakes.visibility = View.GONE
        }

        // We bind the onClickListener of the button btnInfoTask to the action of going to the page myTasks (dashboard)
        val btnInfoTask = dialog.findViewById<TextView>(R.id.btn_info_task)
        btnInfoTask.setOnClickListener {
            val navView = (context as AppCompatActivity).findViewById<BottomNavigationView>(R.id.nav_view)
            navView.selectedItemId = R.id.navigation_dashboard
            dialog.dismiss()
        }

        val btnCancel = dialog.findViewById<TextView>(R.id.btn_cancel)
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}
