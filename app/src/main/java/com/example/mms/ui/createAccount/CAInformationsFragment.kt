package com.example.mms.ui.createAccount

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.mms.R
import com.example.mms.Utils.isEmailValid
import com.example.mms.database.inApp.AppDatabase
import com.example.mms.database.inApp.SingletonDatabase
import com.example.mms.databinding.FragmentCreateAccountInformationsBinding
import com.example.mms.model.User
import kotlinx.coroutines.DelicateCoroutinesApi
import java.util.Calendar

class CAInformationsFragment : Fragment() {

    private var _binding: FragmentCreateAccountInformationsBinding? = null
    private lateinit var db: AppDatabase

    private val binding get() = _binding!!

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val viewModel = ViewModelProvider(requireActivity())[SharedCAViewModel::class.java]

        _binding = FragmentCreateAccountInformationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        db = SingletonDatabase.getDatabase(requireContext())

        // set user's information
        binding.editNom.setText(viewModel.userData.value?.name)
        binding.editPrenom.setText(viewModel.userData.value?.surname)
        binding.editBirthdate.setText(viewModel.userData.value?.birthday ?: "----")
        binding.editTaille.setText(viewModel.userData.value?.height?.toString() ?: "")
        binding.editPoids.setText(viewModel.userData.value?.height?.toString() ?: "")
        //binding.spinnerSexe.setSelection(viewModel.userData.value?.sexe.toString().toInt())

        binding.editBirthdate.keyListener = null
        binding.editBirthdate.isFocusable = false

        binding.editBirthdate.setOnClickListener {
            // init calendar
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // build date picker dialog
            val datePickerDialog = DatePickerDialog(
                root.context,
                DatePickerDialog.OnDateSetListener { view, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    binding.editBirthdate.setText(selectedDate)
                },
                year,
                month,
                day
            )
            // set max date to today
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

            datePickerDialog.show()
        }

        // regex for name and surname
        val nameSurnameRegex = Regex("^[a-zA-ZÀ-ÿ-'\\s]+$")
        val nameSurnameFilter = InputFilter { source, start, end, dest, dstart, dend ->
            if (source != null && !source.toString().matches(nameSurnameRegex)) {
                ""  // Si le texte ne correspond pas à la regex, le caractère est supprimé
            } else {
                null // Sinon, le caractère est accepté
            }
        }

        // fields options
        binding.editNom.filters = arrayOf(nameSurnameFilter)
        binding.editPrenom.filters = arrayOf(nameSurnameFilter)

        binding.editPoids.inputType = InputType.TYPE_CLASS_NUMBER
        binding.editPoids.filters = arrayOf(InputFilter.LengthFilter(3))
        binding.editTaille.inputType = InputType.TYPE_CLASS_NUMBER
        binding.editTaille.filters =  arrayOf(InputFilter.LengthFilter(3))

        binding.buttonSuivant.setOnClickListener {
            // get values from fields
            val name = binding.editNom.text.toString()
            val surname = binding.editPrenom.text.toString()
            val email = binding.editEmail.text.toString()
            val brithDate = binding.editBirthdate.text.toString()
            val sexe = binding.spinnerSexe.selectedItem.toString()
            val weightText = binding.editPoids.text.toString()
            val heightText = binding.editTaille.text.toString()

            val nameRegex = Regex("^[a-zA-ZÀ-ÿ\\s]+$")

            // if data is valid
            if (name.matches(nameRegex) &&
                surname.matches(nameRegex) &&
                brithDate.isNotBlank() &&
                sexe.isNotBlank() &&
                isEmailValid(email)
            ) {
                val weight = weightText.toInt()
                val height = heightText.toInt()

                // create a user object and store it in the view model
                val user = User(name, surname, email, brithDate, sexe, weight, height, true, "", "", "", "", false)
                viewModel.setUserData(user)

                // go to next page
                val navHostFragment =
                    requireActivity().supportFragmentManager.findFragmentById(R.id.nav_create_account) as NavHostFragment
                val navController = navHostFragment.navController
                navController.navigate(R.id.action_info_to_diseases)
            } else {
                Toast.makeText(context, R.string.check_fields, Toast.LENGTH_SHORT).show()
            }
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