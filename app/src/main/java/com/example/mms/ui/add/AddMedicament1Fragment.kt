package com.example.mms.ui.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.mms.R
import com.example.mms.database.inApp.SingletonDatabase
import com.example.mms.databinding.FragmentAddMedicament1Binding
import com.example.mms.model.medicines.Medicine

class AddMedicament1Fragment : Fragment() {

    private var _binding: FragmentAddMedicament1Binding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SharedAMViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        this.viewModel = ViewModelProvider(requireActivity())[SharedAMViewModel::class.java]

        _binding = FragmentAddMedicament1Binding.inflate(inflater, container, false)
        val root: View = binding.root


        var medicines: List<Medicine> = mutableListOf()
        val mapFormesDosagesLiveData = MutableLiveData<MutableMap<String, MutableList<String>>>()

        Thread {

            if (viewModel.medicineName.value!!.isNotEmpty()) {
                val mediDb = SingletonDatabase.getDatabase(requireContext()).medicineDao()
                medicines = mediDb.getMedicinesByName(viewModel.medicineName.value!!)
            }

            val mapFormesDosages = mutableMapOf<String, MutableList<String>>()
            for (medicine in medicines) {
                if (medicine.type.generic == null || medicine.type.weight == null) {
                    continue
                }

                if (mapFormesDosages.containsKey(medicine.type.generic)) {
                    if (!mapFormesDosages[medicine.type.generic]?.contains(medicine.type.weight)!!) {
                        mapFormesDosages[medicine.type.generic]?.add(medicine.type.weight!!)
                    }
                } else {
                    mapFormesDosages[medicine.type.generic!!] =
                        mutableListOf(medicine.type.weight!!)
                }
            }

            requireActivity().runOnUiThread {
                mapFormesDosagesLiveData.value = mapFormesDosages

                binding.editMedicamentNom.setText(viewModel.medicineName.value)

                val formes = mapFormesDosages.keys.toList()

                val arrayAdapterFormes =
                    ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, formes)
                binding.spinnerForme.adapter = arrayAdapterFormes

                // Initialement vide jusqu'à la sélection d'une forme
                val dosages = arrayListOf<String>()

                val arrayAdapterDosages =
                    ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dosages)
                binding.spinnerDosage.adapter = arrayAdapterDosages

                binding.spinnerForme.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            // Mettre à jour les dosages lorsque la forme est sélectionnée
                            dosages.clear()
                            val selectedForm = formes[position]
                            dosages.addAll(mapFormesDosages[selectedForm] ?: emptyList())
                            arrayAdapterDosages.notifyDataSetChanged()
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            // Gérer le cas où rien n'est sélectionné si nécessaire
                        }
                    }
            }
        }.start()

        Thread {
            if (viewModel.taskData.value!!.medicineCIS != 0.toLong()) {

                val mapFormesDosages = mapFormesDosagesLiveData.value ?: mutableMapOf()

                val mediDb = SingletonDatabase.getDatabase(requireContext()).medicineDao()
                val medicine = mediDb.getByCIS(viewModel.taskData.value!!.medicineCIS)

                if (medicine != null) {
                    requireActivity().runOnUiThread {
                        val formes = mapFormesDosages.keys.toList()
                        val selectedFormIndex = formes.indexOf(medicine.type.generic)
                        val dosagesForSelectedForm =
                            mapFormesDosages[medicine.type.generic] ?: emptyList()
                        val selectedDosageIndex =
                            dosagesForSelectedForm.indexOf(medicine.type.weight)

                        binding.spinnerForme.setSelection(selectedFormIndex)
                        binding.spinnerDosage.setSelection(selectedDosageIndex)
                    }
                }
            }
        }.start()


        binding.nextButton.setOnClickListener {
            val forme = binding.spinnerForme.selectedItem.toString()
            val dosage = binding.spinnerDosage.selectedItem.toString()

            if (forme.isEmpty() || dosage.isEmpty()) {
                Toast.makeText(
                    this@AddMedicament1Fragment.context,
                    R.string.fill_fields,
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            Thread {
                val mediDb = SingletonDatabase.getDatabase(this.requireContext()).medicineDao()
                val medicine =
                    mediDb.getByNameTypeWeight(viewModel.medicineName.value!!, forme, dosage)

                if (medicine != null) {
                    val task = viewModel.taskData.value
                    task!!.medicineCIS = medicine.code_cis

                    requireActivity().runOnUiThread {
                        viewModel.setTask(task)

                        val navHostFragment =
                            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_add_medicament) as NavHostFragment
                        val navController = navHostFragment.navController
                        navController.navigate(R.id.action_AM1Fragment_to_AM2_Fragment)
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            this@AddMedicament1Fragment.context,
                            R.string.error_medicine_not_found,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }.start()
        }

        binding.backButton.buttonArrowBack.setOnClickListener {
            requireActivity().finish()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}