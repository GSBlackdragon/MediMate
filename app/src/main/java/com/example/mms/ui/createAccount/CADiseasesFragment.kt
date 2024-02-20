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
        binding.editAllergies.setText(currentUser.listAllergies)


        binding.buttonAddAllergies.setOnClickListener {
            //Selecting the current allergies selected to pass it to the dialog in order to display the current allergies as already selected
            val selectedAllergies = viewModel.userData.value!!.listAllergies.split(",").toList().map { it.trim() }

            val dialog = CustomDialogDiseasses(root.context, listAllergies, selectedAllergies) {
                //Adding back the new selected allergies to the current allergy list of the current user
                currentUser.listAllergies=""
                for (element in it) {
                    currentUser.listAllergies += if (element==it.last()){
                        element
                    }else{
                        "$element, "
                    }
                }
                viewModel.setUserData(currentUser)

                /*
                Displaying selected diseases in-line
                 */
                binding.editAllergies.text = currentUser.listAllergies
            }
            dialog.show()

            //Updating current user allergies to what was selected in the dialog when it dismisses
            dialog.setOnDismissListener {
                val updatedSelectedAllergies =
                    viewModel.userData.value!!.listAllergies.split(",").toList()
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