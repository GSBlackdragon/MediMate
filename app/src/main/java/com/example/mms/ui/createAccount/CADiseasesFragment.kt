package com.example.mms.ui.createAccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.mms.R
import com.example.mms.database.inApp.AppDatabase
import com.example.mms.database.inApp.SingletonDatabase
import com.example.mms.constant.listAllergies
import com.example.mms.constant.listDietPlan
import com.example.mms.constant.listHealthDiseases
import com.example.mms.databinding.FragmentCreateAccountDiseasesBinding
import com.example.mms.ui.createAccount.Dialog.CustomDialogDiseasses

class CADiseasesFragment : Fragment() {

    private var _binding: FragmentCreateAccountDiseasesBinding? = null
    private lateinit var db: AppDatabase


    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val viewModel = ViewModelProvider(requireActivity())[SharedCAViewModel::class.java]
        db = SingletonDatabase.getDatabase(requireContext())
        _binding = FragmentCreateAccountDiseasesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val navHostFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_create_account) as NavHostFragment
        val navController = navHostFragment.navController


        binding.backButton.buttonArrowBack.setOnClickListener {
            navController.navigate(R.id.action_diseases_to_informations)
        }

        binding.buttonSuivant.setOnClickListener {
            navController.navigate(R.id.action_diseases_to_pin)
        }

        val currentUser = viewModel.userData.value!!

        binding.buttonAddHealthDisease.setOnClickListener {
            val selectedHealthDiseases =
                viewModel.userData.value!!.listHealthDiseases.split(",").toList()
            val dialog =
                CustomDialogDiseasses(root.context, listHealthDiseases, selectedHealthDiseases) {
                    it.forEachIndexed { index, healthDisease ->
                        currentUser.listHealthDiseases += if (index < it.size - 1) {
                            "$healthDisease,"
                        } else {
                            healthDisease
                        }
                    }
                    viewModel.setUserData(currentUser)
                    binding.editSoucis.setText(getString(R.string.elements_selectionnes, it.size.toString()))
                }
            dialog.show()

            dialog.setOnDismissListener {
                val updatedSelectedAllergies =
                    viewModel.userData.value!!.listAllergies.split(",").toList()
                dialog.updateSelectedItems(updatedSelectedAllergies)
            }
        }


        binding.buttonAddAllergies.setOnClickListener {
            val selectedAllergies = viewModel.userData.value!!.listAllergies.split(",").toList()
            val dialog = CustomDialogDiseasses(root.context, listAllergies, selectedAllergies) {
                it.forEachIndexed { index, allergie ->
                    currentUser.listAllergies += if (index < it.size - 1) {
                        "$allergie,"
                    } else {
                        allergie
                    }
                }
                viewModel.setUserData(currentUser)
                binding.editAllergies.setText(getString(R.string.elements_selectionnes, it.size.toString()))
            }
            dialog.show()

            dialog.setOnDismissListener {
                val updatedSelectedAllergies =
                    viewModel.userData.value!!.listAllergies.split(",").toList()
                dialog.updateSelectedItems(updatedSelectedAllergies)
            }
        }


        binding.buttonAddDietPlan.setOnClickListener {
            val selectedDietPlan = viewModel.userData.value!!.listDietPlan.split(",").toList()
            val dialog = CustomDialogDiseasses(root.context, listDietPlan, selectedDietPlan) {
                it.forEachIndexed { index, dietPlan ->
                    currentUser.listDietPlan += if (index < it.size - 1) {
                        "$dietPlan,"
                    } else {
                        dietPlan
                    }
                }
                viewModel.setUserData(currentUser)
                binding.editRegime.setText(getString(R.string.elements_selectionnes, it.size.toString()))
            }
            dialog.show()

            dialog.setOnDismissListener {
                val updatedSelectedAllergies = currentUser.listDietPlan.split(",").toList()
                dialog.updateSelectedItems(updatedSelectedAllergies)
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}