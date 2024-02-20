package com.example.mms.ui.add

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
import com.example.mms.databinding.FragmentAddMedicamentCycleBinding
import com.example.mms.model.Cycle

class AddMedicamentCycleFragment: Fragment() {
    private var _binding: FragmentAddMedicamentCycleBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SharedAMViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[SharedAMViewModel::class.java]
        _binding = FragmentAddMedicamentCycleBinding.inflate(inflater, container, false)

        viewModel.setPreviousFragmentId(findNavController().currentDestination!!.id)

        val root: View = binding.root

        val cycle = viewModel.cycle.value
        if (cycle != null && cycle.isNotEmpty()) {
            // 24 to convert hours to days
            binding.inputTreatementDuration.setText((cycle.hoursOfTreatment / 24).toString())
            binding.inputTreatementRest.setText((cycle.hoursOfRest / 24).toString())
        }

        binding.backButton.buttonArrowBack.setOnClickListener {
            goTo(requireActivity(), R.id.action_AMCycle_to_AM2_Fragment)
        }

        binding.nextButton.setOnClickListener {
            // take values from the form
            val cycleDuration = binding.inputTreatementDuration.text.toString()
            val cycleRest = binding.inputTreatementRest.text.toString()

            // check if the values are correct
            val errorMessage = when (true) {
                (cycleDuration == "" || cycleRest == "") -> getString(R.string.fill_fields)
                (cycleDuration.toIntOrNull() == null || cycleRest.toIntOrNull() == null) -> getString(R.string.check_fields)
                (cycleDuration.toInt() < 1) -> getString(R.string.cycle_duration_too_short)
                else -> ""
            }

            // if there is an error, display it and stop the function
            if (errorMessage.isNotBlank()) {
                Toast.makeText(root.context, errorMessage, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // set values in the view model
            // * 24 to convert days to hours
            val cycle = Cycle(
                0, 0,
                cycleDuration.toInt() * 24,
                0,
                cycleRest.toInt() * 24
            )
            viewModel.setCycle(cycle)

            goTo(requireActivity(), R.id.action_AMCycle_to_AMCycleHourWeight)
        }

        return root
    }
}
