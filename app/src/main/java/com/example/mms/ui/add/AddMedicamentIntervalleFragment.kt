package com.example.mms.ui.add

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mms.R
import com.example.mms.Utils.goTo
import com.example.mms.Utils.hourMinuteToString
import com.example.mms.Utils.stringHourMinuteToInt
import com.example.mms.databinding.FragmentAddMedicamentPlusIntervalleBinding
import com.example.mms.model.Cycle
import com.example.mms.service.TasksService

class AddMedicamentIntervalleFragment : Fragment() {
    private var _binding: FragmentAddMedicamentPlusIntervalleBinding? = null
    private val binding get() = _binding!!
    private var beginHour = hourMinuteToString(8, 0)
    private var endHour = hourMinuteToString(22, 0)
    private lateinit var tasksService: TasksService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel = ViewModelProvider(requireActivity())[SharedAMViewModel::class.java]
        this.tasksService = TasksService(requireContext())

        viewModel.setPreviousFragmentId(findNavController().currentDestination!!.id)

        _binding = FragmentAddMedicamentPlusIntervalleBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // set text and options
        binding.textTimepickerBeginHour.text = this.beginHour
        binding.textTimepickerEndHour.text = this.endHour

        binding.numberPicker.value = 1
        binding.numberPicker.minValue = 1
        binding.numberPicker.maxValue = 24
        binding.numberPicker.wrapSelectorWheel = true

        binding.numberPickerWeight.setText("1")

        // Time picker
        binding.textTimepickerBeginHour.setOnClickListener {
            openTimePicker(true)
        }

        binding.textTimepickerEndHour.setOnClickListener {
            openTimePicker(false)
        }

        binding.backButton.buttonArrowBack.setOnClickListener {
            viewModel.clearFrequencyData()
            goTo(requireActivity(), R.id.action_AMIntervalle_to_AMPlus)
        }

        binding.nextButton.setOnClickListener {
            // check if fields are filled
            if (binding.numberPicker.value == 0 || binding.numberPickerWeight.text.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.fill_fields),
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // check if begin hour is before end hour
            val hourWeightList = this.tasksService.generateHourWeightForInterval(
                binding.numberPicker.value,
                stringHourMinuteToInt(this.beginHour),
                stringHourMinuteToInt(this.endHour),
                binding.numberPickerWeight.text.toString().toInt()
            )

            // store the interval
            val cycle = Cycle(0,0,24,0,0, hourWeightList)
            viewModel.setCycle(cycle)

            goTo(requireActivity(), R.id.action_AMIntervalle_to_AMStorage)
        }

        return root
    }

    /**
     * Open the time picker dialog
     *
     * @param isBeginHour true if the time picker is for the begin hour, false otherwise
     */
    private fun openTimePicker(isBeginHour: Boolean) {
        val hourToShowString = if (isBeginHour) {
            this.beginHour
        } else {
            this.endHour
        }

        // get the hour and minute from the string
        val splited = hourToShowString.split(":")
        val hour = splited[0].toInt()
        val minute = splited[1].toInt()

        // define the dialog
        val dialog = TimePickerDialog(
            this.requireContext(), { _, hourOfDay, minuteOfDay ->
                val hourMinuteString = hourMinuteToString(hourOfDay, minuteOfDay)

                if (isBeginHour) {
                    binding.textTimepickerBeginHour.text = hourMinuteString
                    this@AddMedicamentIntervalleFragment.beginHour = hourMinuteString
                } else {
                    binding.textTimepickerEndHour.text = hourMinuteString
                    this@AddMedicamentIntervalleFragment.endHour = hourMinuteString
                }

            }, hour, minute, true
        )

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
