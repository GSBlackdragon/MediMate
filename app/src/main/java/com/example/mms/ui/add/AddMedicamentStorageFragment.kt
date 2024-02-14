package com.example.mms.ui.add

import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mms.R
import com.example.mms.Utils.goTo
import com.example.mms.database.inApp.AppDatabase
import com.example.mms.database.inApp.SingletonDatabase
import com.example.mms.databinding.FragmentAddMedicamentStorageBinding
import com.example.mms.model.MedicineStorage

class AddMedicamentStorageFragment : Fragment() {

    private var _binding: FragmentAddMedicamentStorageBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SharedAMViewModel
    private lateinit var db : AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentAddMedicamentStorageBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel = ViewModelProvider(requireActivity())[SharedAMViewModel::class.java]
        db = SingletonDatabase.getDatabase(requireContext())

        var medicineStorage = viewModel.storage.value

        var isInDb = false
        val t = Thread {
            val medicineId = db.medicineDao().getMedicineIdByName(viewModel.medicineName.value!!)
            medicineStorage = db.medicineStorageDao().getMedicineStorageByMedicineId(medicineId)
            if (medicineStorage != null) isInDb = true
        }
        t.start()
        t.join()

        // set options
        binding.switch1.isChecked = false
        binding.tvAlreadyStored.visibility = View.GONE
        binding.constraintLayoutStorage.getViewById(R.id.edit_alert_storage).isEnabled = false
        binding.constraintLayoutStorage.getViewById(R.id.edit_actual_storage).isEnabled = false


        // set storage informations
        Log.d("cc",medicineStorage.toString())
        if (medicineStorage != null) {
            if (isInDb) binding.tvAlreadyStored.visibility = View.VISIBLE

            binding.switch1.isChecked = true
            binding.constraintLayoutStorage.getViewById(R.id.edit_alert_storage).isEnabled = true
            binding.constraintLayoutStorage.getViewById(R.id.edit_actual_storage).isEnabled = true
            val storage = binding.constraintLayoutStorage.getViewById(R.id.edit_actual_storage) as EditText
            storage.setText(medicineStorage!!.storage.toString())
            val alertValue = binding.constraintLayoutStorage.getViewById(R.id.edit_alert_storage) as EditText
            alertValue.setText(medicineStorage!!.alertValue.toString())
        }


        binding.backButton.root.setOnClickListener {
            // get back to the previous fragment
            findNavController().popBackStack(viewModel.previousFragmentId.value!!, false)
        }


        binding.switch1.setOnCheckedChangeListener { _, isChecked ->
            // change the style of the layout depending on the switch
            if (isChecked) {
                binding.constraintLayoutStorage.setBackgroundColor(Color.WHITE)
                binding.constraintLayoutStorage.getViewById(R.id.edit_alert_storage).isEnabled = true
                binding.constraintLayoutStorage.getViewById(R.id.edit_actual_storage).isEnabled = true
            } else {
                binding.constraintLayoutStorage.setBackgroundColor(resources.getColor(R.color.light_gray))
                binding.constraintLayoutStorage.getViewById(R.id.edit_alert_storage).isEnabled = false
                binding.constraintLayoutStorage.getViewById(R.id.edit_actual_storage).isEnabled = false
            }
        }

        binding.editActualStorage.inputType = InputType.TYPE_CLASS_NUMBER
        binding.editAlertStorage.inputType = InputType.TYPE_CLASS_NUMBER

        binding.nextButton.setOnClickListener {
            if (binding.switch1.isChecked) {
                val storage = binding.constraintLayoutStorage.getViewById(R.id.edit_actual_storage) as EditText
                val alertValue = binding.constraintLayoutStorage.getViewById(R.id.edit_alert_storage) as EditText

                if (storage.text.toString().isNotEmpty() && alertValue.text.toString().isNotEmpty()) {
                    // get the id of the medicine
                    var medicineId : Long = 0
                    val tt = Thread {
                        medicineId = db.medicineDao().getMedicineIdByName(viewModel.medicineName.value!!)
                    }
                    tt.start()
                    tt.join()

                    val obj = MedicineStorage(
                        medicineId,
                        storage.text.toString().toInt(),
                        alertValue.text.toString().toInt()
                    )

                    // save the storage informations
                    viewModel.setStorage(obj)
                    goTo(requireActivity(), R.id.action_storage_to_start_end_date)
                }else{
                    Toast.makeText(
                        this.requireContext(),
                        this.requireContext().getString(R.string.storage_continuer),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }else{
                goTo(requireActivity(), R.id.action_storage_to_start_end_date)
            }
        }

        return root

    }


}