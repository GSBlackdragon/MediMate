package com.example.mms.ui.add

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mms.MainActivity
import com.example.mms.R
import com.example.mms.Utils.OCR
import com.example.mms.adapter.Interface.OnItemClickListener
import com.example.mms.adapter.MediChooseAdapter
import com.example.mms.contrat.AddTaskFromOCR
import com.example.mms.database.inApp.AppDatabase
import com.example.mms.database.inApp.SingletonDatabase
import com.example.mms.databinding.ActivityChooseMedicamentBinding

class ChooseMedicamentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChooseMedicamentBinding
    private lateinit var medicamentsFound : List<OCR.MedicationInfo>
    private lateinit var hashmapTaskId : HashMap<Int,Long>
    private var contratLaunchedPosition = -1

    private lateinit var adapter: MediChooseAdapter
    private lateinit var db : AppDatabase

    private var contratAddFromOCR : ActivityResultLauncher<String?> = registerForActivityResult(
        AddTaskFromOCR()
    ) {
        Log.d("ChooseMedicamentActivity", "taskId: $it $contratLaunchedPosition")
        if (it != -1L && contratLaunchedPosition != -1) {
            hashmapTaskId[contratLaunchedPosition] = it
            Log.d("ChooseMedicamentActivity", "taskId: $it $contratLaunchedPosition")
            adapter.notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        binding = ActivityChooseMedicamentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val backButton = binding.backButton.root
        backButton.setOnClickListener {
            finish()

            // Delete all tasks created
            for (taskId in hashmapTaskId.values) {
                deleteTask(taskId)
            }
        }


        binding.btnNext.setOnClickListener {
            if (hashmapTaskId.size == medicamentsFound.size) {
                this.goToMain()
            } else {
                this.dialogToSaveOnlySelectedMedicine()
            }
        }

        db = SingletonDatabase.getDatabase(this)

        medicamentsFound = intent.extras?.getParcelableArrayList<OCR.MedicationInfo>("medicamentFound") as List<OCR.MedicationInfo>
        hashmapTaskId = HashMap()

        val listMedicamentView = binding.medicamentRecyclerView
        adapter = MediChooseAdapter(this, db, medicamentsFound, hashmapTaskId)

        if (medicamentsFound.isEmpty()) {
            Toast.makeText(this, getString(R.string.aucun_medicament_trouve), Toast.LENGTH_SHORT).show()
            this.goToMain()
        } else {
            listMedicamentView.layoutManager = LinearLayoutManager(this)
            listMedicamentView.adapter = adapter

            adapter.setOnItemClickListener(object : OnItemClickListener {
                override fun onItemClick(position: Int) {
                    contratLaunchedPosition = position
                    contratAddFromOCR.launch(medicamentsFound[position].name)
                }
            })
        }
    }

    private fun deleteTask(taskId: Long) {
        val tt = Thread {
            db.taskDao().deleteById(taskId)
        }
        tt.start()
        tt.join()
    }

    private fun dialogToSaveOnlySelectedMedicine() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle(this.getString(R.string.confirmation))

        val nbMedicament = hashmapTaskId.size
        if (nbMedicament == 1) {
            builder.setMessage(this.getString(R.string.confirmation_message_ocr_add, nbMedicament.toString()))
        } else {
            builder.setMessage(this.getString(R.string.confirmation_message_ocr_add_pluriel, nbMedicament.toString()))
        }

        builder.setPositiveButton(this.getString(R.string.oui)) { dialog, which ->
            this.goToMain()
        }

        builder.setNegativeButton(this.getString(R.string.non)) { dialog, which ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)

    }
}
