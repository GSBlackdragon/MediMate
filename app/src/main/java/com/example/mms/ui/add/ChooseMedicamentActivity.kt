package com.example.mms.ui.add

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mms.MainActivity
import com.example.mms.R
import com.example.mms.Utils.OCR
import com.example.mms.adapter.MediChooseAdapter
import com.example.mms.adapter.TempDoctorAdapter
import com.example.mms.contrat.AddTaskFromOCR
import com.example.mms.database.inApp.AppDatabase
import com.example.mms.database.inApp.SingletonDatabase
import com.example.mms.databinding.ActivityChooseMedicamentBinding
import com.example.mms.model.Doctor
import com.example.mms.service.ApiService

class ChooseMedicamentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChooseMedicamentBinding
    private lateinit var medicamentsFound : List<OCR.MedicationInfo>
    private lateinit var doctorFound : Array<String>
    private lateinit var hashmapTaskId : HashMap<Int,Long>
    private var contratLaunchedPosition = -1

    private lateinit var adapterMedicine: MediChooseAdapter
    private lateinit var adapterDoctor: TempDoctorAdapter
    private lateinit var db : AppDatabase

    private var contratAddFromOCR : ActivityResultLauncher<String?> = registerForActivityResult(
        AddTaskFromOCR()
    ) {
        Log.d("ChooseMedicamentActivity", "taskId: $it $contratLaunchedPosition")
        if (it != -1L && contratLaunchedPosition != -1) {
            hashmapTaskId[contratLaunchedPosition] = it
            Log.d("ChooseMedicamentActivity", "taskId: $it $contratLaunchedPosition")
            adapterMedicine.notifyDataSetChanged()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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

        val doctorToAdd = mutableListOf<Doctor>()

        binding.btnNext.setOnClickListener {
            if (hashmapTaskId.size == medicamentsFound.size) {
                this.goToMain()
            } else {
                if (doctorToAdd.isNotEmpty()){
                    val db = SingletonDatabase.getDatabase(this)
                    val tt = Thread {
                        val doctorDao = db.doctorDao()
                        doctorToAdd.forEach {
                            doctorDao.insert(it)
                        }
                    }
                    tt.join()
                    tt.start()
                }
                this.dialogToSaveOnlySelectedMedicine()
            }
        }

        db = SingletonDatabase.getDatabase(this)
        medicamentsFound = intent.extras?.getParcelableArray("medicamentFound", OCR.MedicationInfo::class.java)?.toList() ?: emptyList()
        doctorFound = intent.extras?.getStringArray("doctorFound") ?: emptyArray()
        hashmapTaskId = HashMap()

        val listMedicamentView = binding.medicamentRecyclerView
        adapterMedicine = MediChooseAdapter(this, db, medicamentsFound, hashmapTaskId)

        val listDoctorRv = binding.doctorDetectedRecyclerView
        if (doctorFound.isEmpty()){
            Toast.makeText(this, getString(R.string.aucun_docteur_trouve), Toast.LENGTH_SHORT).show()
            listDoctorRv.visibility = View.GONE
            binding.tvDoctorFound.text = getString(R.string.aucun_docteur_trouve)
        }else{
            val doctorList = mutableListOf<Doctor>()
            val api = ApiService.getInstance(this)
            doctorFound.forEach {
                api.getDoctor(null,it, object : ApiService.DoctorResultCallback {
                    override fun onSuccess(doctors: List<Doctor>?) {
                        if (doctors != null) {
                            doctorList.addAll(doctors)
                            adapterDoctor.notifyDataSetChanged()
                        }
                    }

                    override fun onError(error: String) {
                        Toast.makeText(this@ChooseMedicamentActivity, error, Toast.LENGTH_SHORT).show()
                    }

                })
            }
            adapterDoctor = TempDoctorAdapter(doctorList, this)
            listDoctorRv.layoutManager = LinearLayoutManager(this)
            listDoctorRv.adapter = adapterDoctor
            adapterDoctor.setOnItemClickListener { position ->
                if (doctorToAdd.contains(doctorList[position])) {
                    doctorToAdd.remove(doctorList[position])
                } else {
                    doctorToAdd.add(doctorList[position])
                }
            }
        }

        if (medicamentsFound.isEmpty()) {
            Toast.makeText(this, getString(R.string.aucun_medicament_trouve), Toast.LENGTH_SHORT).show()
            this.goToMain()
        } else {
            listMedicamentView.layoutManager = LinearLayoutManager(this)
            listMedicamentView.adapter = adapterMedicine

            adapterMedicine.setOnItemClickListener { position ->
                contratLaunchedPosition = position
                contratAddFromOCR.launch(medicamentsFound[position].name)
            }
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

        builder.setPositiveButton(this.getString(R.string.oui)) { _, _ ->
            this.goToMain()
        }

        builder.setNegativeButton(this.getString(R.string.non)) { dialog, _ ->
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
