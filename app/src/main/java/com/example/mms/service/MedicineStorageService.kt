package com.example.mms.service

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import com.example.mms.R
import com.example.mms.adapter.TakesAdapter
import com.example.mms.database.inApp.SingletonDatabase
import com.example.mms.model.MedicineStorage
import com.google.android.material.snackbar.Snackbar


/**
 * Service to manage the storage of each medicines
 *
 * @property context The context of the application
 * @property view The view of the activity
 * @property db The local database
 */
class MedicineStorageService(val context : Context, val view : View?) {
    private var db = SingletonDatabase.getDatabase(context)


    fun getMedicineStorageByMedicineId(medicineId : Long) : MedicineStorage?{
        return db.medicineStorageDao().getMedicineStorageByMedicineId(medicineId)
    }
    /**
     * Update the storage of a medicine
     * If the storage is low, alert the user
     * If the storage is not enough, alert the user and ask him if he wants to update the storage or cancel the takes
     *
     * @param medicineStorage The medicine storage to update
     * @param weight The weight of the takes
     * @param medicineName The name of the medicine
     * @param takesAdapter The adapter of the takes
     *
     * @return true if the user dismiss the dialog, false otherwise
     */
    fun updateMedicineStorage(medicineStorage: MedicineStorage, weight : Int, medicineName : String, takesAdapter: TakesAdapter) : Boolean {
        // The storage - the weight of the takes
        val res = medicineStorage.storage - weight
        var update = false
        var dismiss = false
        when {
            // If the storage is not enough
            res < 0 -> {
                // Alert -> not enough storage for this takes -> update storage or cancel takes
                android.os.Handler(Looper.getMainLooper()).post {
                    val dialog = AlertDialog.Builder(context)

                    // set message of alert dialog
                    dialog.setTitle(this.context.getString(R.string.stock_insuffisant))
                    dialog.setMessage(this.context.getString(R.string.vous_avez_plus_beaucoup_doses))

                    // set action buttons
                    dialog.setPositiveButton(this.context.getString(R.string.mettre_jour_stock)) { dialog, _ ->
                        dialogGererStock(medicineStorage, medicineName, takesAdapter)
                        dismiss = true
                        dialog.dismiss()
                    }

                    dialog.setNegativeButton(this.context.getString(R.string.annuler_la_prise)) { dialog, _ ->
                        dismiss = true
                        dialog.dismiss()
                    }

                    dialog.setCancelable(true)
                    dialog.setOnCancelListener { dialog ->
                        dismiss = true
                        dialog.dismiss()
                    }

                    val alertDialog = dialog.create()
                    alertDialog.show()
                }

            }
            // If the storage is low
            res <= medicineStorage.alertValue -> {
                // update the storage
                medicineStorage.storage = res
                update = true
                // Alert -> you stock is low -> buy more
                val snackbar = Snackbar.make(
                    view!!,
                    this.context.getString(R.string.stock_faible_pensez_racheter),
                    Snackbar.LENGTH_LONG)

                // Add a button to dismiss the snackbar
                snackbar.setAction("X") {
                    snackbar.dismiss()
                }

                // Add a button to update the storage
                snackbar.setAction(this.context.getString(R.string.gerer_stock)) {
                    dialogGererStock(medicineStorage, medicineName, takesAdapter)
                    snackbar.dismiss()
                }

                // Change the color of the snackbar
                snackbar.setBackgroundTint(context.getColor(R.color.white))
                snackbar.setTextColor(context.getColor(R.color.black))

                // Position the snackbar on top
                val view = snackbar.view
                val params = view.layoutParams as FrameLayout.LayoutParams
                params.gravity = Gravity.TOP
                params.topMargin = 100
                view.layoutParams = params

                snackbar.show()

                // Dismiss the snackbar after 10 seconds
                val handler = android.os.Handler(Looper.getMainLooper())
                handler.postDelayed({
                    snackbar.dismiss()
                }, 10000)
            }
            // If the storage is enough
            else -> {
                medicineStorage.storage = res
                update = true
            }
        }
        // Update the storage in the database
        val tt = Thread {
            this.db.medicineStorageDao().insert(medicineStorage)
        }
        if (update) {
            tt.start()
            tt.join()
        }
        return dismiss
    }

    /**
     * Show a dialog to update the storage of a medicine
     *
     * @param mStorage The medicine storage to update
     * @param medicineName The name of the medicine
     * @param adapter The adapter of the takes
     */
    fun dialogGererStock(mStorage: MedicineStorage, medicineName: String, adapter: TakesAdapter) {
        // dialog popup
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.custom_dialog_edit_storage)

        // set text
        val tvMedicineName = dialog.findViewById<TextView>(R.id.tv_info_takes)
        tvMedicineName.text = medicineName
        val editStorage = dialog.findViewById<EditText>(R.id.edit_actual_storage)
        editStorage.setText(mStorage.storage.toString())
        val editAlertValue = dialog.findViewById<EditText>(R.id.edit_alert_storage)
        editAlertValue.setText(mStorage.alertValue.toString())

        // cancel button
        val btnCancel = dialog.findViewById<TextView>(R.id.btn_cancel)
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // update button
        val btnUpdate = dialog.findViewById<TextView>(R.id.btn_save)
        btnUpdate.setOnClickListener {
            // get the new storage and alert value
            val storage = editStorage.text.toString().toInt()
            val alertValue = editAlertValue.text.toString().toInt()

            // update the storage and alert value
            mStorage.storage = storage
            mStorage.alertValue = alertValue

            // insert the new storage and alert value in the database
            val tt = Thread {
                this.db.medicineStorageDao().update(mStorage)
            }
            tt.start()
            tt.join()

            // update the adapter
            adapter.notifyDataSetChanged()
            dialog.dismiss()
        }

        dialog.show()
    }
}