package com.example.mms.ui.add

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mms.R
import com.example.mms.Utils.dateToString
import com.example.mms.Utils.goTo
import com.example.mms.databinding.FragmentAddMedicamentStartEndDateBinding
import java.time.LocalDateTime
import java.util.Calendar

class AddMedicamentDateDebutFinFragment : Fragment() {

    private var _binding: FragmentAddMedicamentStartEndDateBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SharedAMViewModel

    private var beginDate: LocalDateTime = LocalDateTime.now()
    private var endDate: LocalDateTime = LocalDateTime.now()

    private var STOP_ON_STOCK : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddMedicamentStartEndDateBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel = ViewModelProvider(requireActivity())[SharedAMViewModel::class.java]


        binding.switchStopOnStock.isChecked = viewModel.taskData.value!!.stopOnStock
        if (binding.switchStopOnStock.isChecked){
            binding.editEndDate.isActivated=true
            STOP_ON_STOCK=true
            binding.editEndDate.setBackgroundResource(R.color.gray)

        }else{
            binding.editEndDate.isActivated=false
            STOP_ON_STOCK=false
            binding.editEndDate.setBackgroundResource(androidx.transition.R.drawable.abc_edit_text_material)

        }

        binding.backButton.root.setOnClickListener {
            // get back to the previous fragment
            goTo(requireActivity(), R.id.action_start_end_date_to_storage)
        }

        // init form
        val dateString = dateToString(beginDate.dayOfMonth, beginDate.monthValue, beginDate.year)
        binding.editBeginDate.setText(dateString)
        binding.editEndDate.setText(binding.editBeginDate.text.toString())

        // setup date picker dialog
        setupDatePickerDialog(binding.editBeginDate, beginDate, true, binding.editEndDate)
        setupDatePickerDialog(binding.editEndDate, endDate, false, binding.editBeginDate)

        if (viewModel.storageIsChecked.value!!){
            binding.switchStopOnStock.visibility=View.VISIBLE
            binding.textStopOnStock.visibility=View.VISIBLE
        }else{
            binding.switchStopOnStock.visibility=View.GONE
            binding.textStopOnStock.visibility=View.GONE
        }

        binding.switchStopOnStock.setOnClickListener {
            if (binding.switchStopOnStock.isChecked){
                binding.editEndDate.isActivated=true
                STOP_ON_STOCK=true
                binding.editEndDate.setBackgroundResource(R.color.gray)

            }else{
                binding.editEndDate.isActivated=false
                STOP_ON_STOCK=false
                binding.editEndDate.setBackgroundResource(androidx.transition.R.drawable.abc_edit_text_material)

            }
        }

        binding.nextButton.setOnClickListener {
            // take values from the form
            val beginDate = binding.editBeginDate.text.toString()
            val endDate = binding.editEndDate.text.toString()

            // check if the values are correct
            val errorMessage = when (true) {
                (beginDate == "") -> getString(R.string.fill_fields)
                (endDate == "") -> getString(R.string.fill_fields)
                else -> null
            }

            if (errorMessage != null) {
                // display error message
                val toast = Toast.makeText(root.context, errorMessage, Toast.LENGTH_SHORT)
                toast.show()
            } else {
                // save values in the view model
                viewModel.taskData.value!!.stopOnStock=STOP_ON_STOCK
                viewModel.taskData.value!!.startDate = this.beginDate
                if (STOP_ON_STOCK){
                    var totalStock = viewModel.storage.value?.storage
                    var totalWeight = viewModel.cycle.value?.hourWeights!!.map { it.weight }.sum()
                    var totalDaysToAdd = totalStock?.div(totalWeight)
                    viewModel.taskData.value!!.endDate = this.beginDate.plusDays(totalDaysToAdd!!.toLong()-1)

                }else{
                    viewModel.taskData.value!!.endDate = this.endDate
                }


                // go to next fragment
                goTo(requireActivity(), R.id.action_start_end_date_to_recap)
            }
        }

        return root
    }

    /**
     * Setup the date picker dialog for the given EditText
     *
     * @param editText the EditText to setup
     * @param initialDate the initial date to display in the EditText
     * @param start true if the EditText is for the start date, false if it's for the end date
     * @param editText2 the other EditText
     */
    fun setupDatePickerDialog(editText: EditText, initialDate: LocalDateTime, start : Boolean, editText2: EditText)  {
        // init date
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // define the date picker dialog
        val datePickerDialog = DatePickerDialog(
            editText.context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDateString = dateToString(selectedDay, selectedMonth + 1, selectedYear)
                val selected = LocalDateTime.of(selectedYear, selectedMonth + 1, selectedDay, 0, 0)
                editText.setText(selectedDateString)
                if (start){
                    beginDate = selected
                    if (endDate.isBefore(beginDate)) {
                        editText2.setText(selectedDateString)
                    }
                }
                else endDate = selected
            },
            year,
            month,
            day
        )

        // if the date is for the end date, set the min date to the begin date
        if (!start) {
            datePickerDialog.datePicker.minDate = beginDate.toLocalDate().toEpochDay() * 24 * 60 * 60 * 1000
        }

        // when the user click on the EditText, show the date picker dialog
        editText.setOnClickListener {
                if (!start) {
                    datePickerDialog.datePicker.minDate = beginDate.toLocalDate().toEpochDay() * 24 * 60 * 60 * 1000
                    if (!STOP_ON_STOCK){
                        datePickerDialog.show()
                    }
                }else {
                    datePickerDialog.show()
                }

        }

        // Afficher la date initiale dans l'EditText
        editText.setText(dateToString(initialDate.dayOfMonth, initialDate.monthValue, initialDate.year))

        editText.isEnabled = true
        editText.keyListener = null
        editText.isFocusable = false
    }

}