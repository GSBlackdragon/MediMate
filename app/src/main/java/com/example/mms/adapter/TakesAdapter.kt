package com.example.mms.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.example.mms.service.SideInfoService
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

    private var SECURITY_LINK : String? = null
    private var HELP_LINK : String? = null
    private val tasksService = TasksService(context)
    private val mStorageService = MedicineStorageService(context,view)
    private val sideInfoService = SideInfoService(context)


    /**
     * Class that represents the view holder of the recycler view
     * @param itemView the view
     */
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskTitle: TextView = itemView.findViewById(R.id.medicine_title)
        val medicineImage  : ImageView = itemView.findViewById(R.id.medicine_image)
        val medicineInformation : TextView = itemView.findViewById(R.id.medicine_information)
        val medicineTypeGeneric : TextView = itemView.findViewById(R.id.medicine_type_generic)
        val taskTime : TextView = itemView.findViewById(R.id.medicine_home_time)
        val timeRemaining : TextView = itemView.findViewById(R.id.medicine_home_time_remaining)
        val buttonTaskChecked : FloatingActionButton = itemView.findViewById(R.id.medicine_home_check)
        val tvStock : TextView = itemView.findViewById(R.id.tv_stock)
        val stock : TextView = itemView.findViewById(R.id.stock_value)
        val alertImage : ImageView = itemView.findViewById(R.id.alertSameSubstance)
        val recyclerViewIconsWarning : RecyclerView = itemView.findViewById(R.id.warningIconsRecyclerView)
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
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = this.items[position]
        context.run {
            holder.taskTitle.text = item.medicineName
        }

        //Horizontal layout for the warning icons
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        /*Gathering all informations about the current warnings set on the specific medicine and checking
         if the currentUser is allergic to the medicine in any way to display warning icons later
         */
        var itemWarnings : List<String> = mutableListOf()
        var isUserAllergic : Pair<Boolean,String> = Pair(false,"")
        val warningThread = Thread {
            val itemWarningsTemp : MutableList<String> = db.sideInfoMedicineDao().getById(item.task.medicineCIS.toString())?.warning!!.split(",").toMutableList()
            isUserAllergic = sideInfoService.knowIfMedicineIsInAllergicListOfUser(item.task.medicineCIS)
            if (isUserAllergic.first){
                itemWarningsTemp.add("allergie")
            }
            itemWarnings = itemWarningsTemp.map { it.trim().lowercase() }.distinct()
        }
        warningThread.start()
        warningThread.join()
        val warningsAdapter = TakesWarningsAdapter(context, itemWarnings.toList(),isUserAllergic.second)

        holder.recyclerViewIconsWarning.layoutManager = layoutManager
        holder.recyclerViewIconsWarning.adapter=warningsAdapter

        //Retrieving active substance code of the current drug to check if there is any active substance duplication
        var itemSubActCode : Int? = null
        val t = Thread {
            itemSubActCode = db.medicineDao().getByCIS(item.task.medicineCIS)?.composition?.substance_code
        }
        t.start()
        t.join()

        //Displaying, if necessary, the "active substance duplication" warning icon
        if (itemSubActCode != null && itemSubActCode in listSubActiveDoublons){
            holder.alertImage.visibility=View.VISIBLE
        }else{
            holder.alertImage.visibility=View.GONE
        }

        //Toast when the warning icon is clicked
        holder.alertImage.setOnLongClickListener {
            Toast.makeText(context, R.string.toast_sub_active, Toast.LENGTH_LONG).show()
            true
        }
        holder.medicineInformation.text = item.task.type
        holder.medicineTypeGeneric.text = "${item.hourWeight.weight} dose"
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

        when (item.medicineType.generic!!) {
            "comprime" -> holder.medicineImage.setImageResource(R.drawable.comprime)
            "suppositoire" -> holder.medicineImage.setImageResource(R.drawable.suppositoire)
            "solution buvable" -> holder.medicineImage.setImageResource(R.drawable.sirop)
            "suspension" -> holder.medicineImage.setImageResource(R.drawable.pipette)
            "gelule" -> holder.medicineImage.setImageResource(R.drawable.gelule)
            else -> holder.medicineImage.setImageResource(R.drawable.medicaments)
        }

        holder.medicineImage.setOnClickListener {
            dialogMedicineInformations(item)
        }
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

    private fun dialogMedicineInformations(item : ShowableHourWeight){

        val dialog = Dialog(context)
        dialog.setContentView(R.layout.custom_dialog_medicine_informations)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.height = (context.resources.displayMetrics.heightPixels * 0.8).toInt()

        dialog.window?.attributes = layoutParams

        val medicineName = dialog.findViewById<TextView>(R.id.medicine_name)
        val medicineType = dialog.findViewById<TextView>(R.id.medicine_type)
        val medicineWeight = dialog.findViewById<TextView>(R.id.medicine_weight)
        val medicineAdministration = dialog.findViewById<TextView>(R.id.medicine_administration)
        val medicineHelpLink = dialog.findViewById<TextView>(R.id.medicine_help_link)
        val medicineSubstance = dialog.findViewById<TextView>(R.id.medicine_substance)
        val medicineSecurityStartDate = dialog.findViewById<TextView>(R.id.medicine_security_start_date)
        val medicineSecurityEndDate = dialog.findViewById<TextView>(R.id.medicine_security_end_date)
        val medicineSecurityLink = dialog.findViewById<TextView>(R.id.medicine_security_link)
        val medicineSales = dialog.findViewById<TextView>(R.id.medicine_sales)
        val medicinePrice = dialog.findViewById<TextView>(R.id.medicine_price)

        val medicineNameTitle = dialog.findViewById<TextView>(R.id.section_med_name)
        val medicineAdministrationTitle = dialog.findViewById<TextView>(R.id.section_med_admin)
        val medicineHelpLinkTitle = dialog.findViewById<TextView>(R.id.section_med_helplink)
        val medicineSubstanceTitle = dialog.findViewById<TextView>(R.id.section_med_sub_act)
        val medicineSecurityStartDateTitle = dialog.findViewById<TextView>(R.id.section_med_start_date)
        val medicineSecurityEndDateTitle = dialog.findViewById<TextView>(R.id.section_med_end_date)
        val medicineSecurityLinkTitle = dialog.findViewById<TextView>(R.id.section_med_secu_link)
        val medicineSalesTitle = dialog.findViewById<TextView>(R.id.section_med_sales)
        val medicinePriceTitle = dialog.findViewById<TextView>(R.id.section_med_issale)



        medicineName.text = item.medicineName
        medicineType.text = item.medicineType.generic
        medicineWeight.text = item.hourWeight.weight.toString()


        val t = Thread {
            var medicine = db.medicineDao().getByCIS(
                item.task.medicineCIS)
            medicineAdministration.text=medicine?.usage?.route_administration
            HELP_LINK=medicine?.usage?.link_help
            medicineSubstance.text=medicine?.composition?.substance_name
            medicineSecurityStartDate.text=medicine?.security_informations?.start_date
            medicineSecurityEndDate.text=medicine?.security_informations?.end_date
            SECURITY_LINK=medicine?.security_informations?.text
            medicineSales.text=medicine?.sales_info?.holder
            medicinePrice.text= if (medicine?.sales_info?.is_on_sale == true) "Disponible à la vente" else "Indisponible à la vente"
        }
        t.start()
        t.join()

        when {
            medicineAdministration.text==null -> medicineAdministrationTitle.visibility=View.GONE
            medicineSubstance.text==null -> medicineSubstanceTitle.visibility=View.GONE
            medicineSecurityStartDate.text==null -> medicineSecurityStartDateTitle.visibility=View.GONE
            medicineSecurityEndDate.text==null -> medicineSecurityEndDateTitle.visibility=View.GONE
            medicineSales.text==null -> medicineSalesTitle.visibility=View.GONE
            medicinePrice.text==null -> medicinePriceTitle.visibility=View.GONE
        }

        if (HELP_LINK!=null && Patterns.WEB_URL.matcher(HELP_LINK).matches()){
            medicineSecurityLink.text=context.getString(R.string.lien_aide)
            medicineHelpLinkTitle.visibility=View.VISIBLE
            medicineHelpLink.visibility=View.VISIBLE
            medicineHelpLink.setOnClickListener {
                // open link into browser
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(HELP_LINK)
                context.startActivity(i)
            }
        }else{
            medicineHelpLinkTitle.visibility=View.GONE
            medicineHelpLink.visibility=View.GONE
        }



        if (SECURITY_LINK!=null && Patterns.WEB_URL.matcher(SECURITY_LINK).matches()){
            medicineSecurityLink.text=context.getString(R.string.lien_securite)
            medicineSecurityLinkTitle.visibility=View.VISIBLE
            medicineSecurityLink.visibility=View.VISIBLE
            medicineSecurityLink.setOnClickListener {
                // open link into browser
                val intent = Intent(Intent.ACTION_VIEW,Uri.parse(SECURITY_LINK))
                context.startActivity(intent)
            }
        }else{
            medicineNameTitle.visibility=View.GONE
            medicineSecurityLink.visibility=View.GONE
        }






        dialog.show()
    }

}
