package com.example.mms.ui.add

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mms.R
import com.example.mms.Utils.goTo
import com.example.mms.Utils.hourMinuteToString
import com.example.mms.adapter.HourWeightAdapter
import com.example.mms.adapter.Interface.OnItemClickListener
import com.example.mms.databinding.FragmentAddMedicamentPlusJoursSpecifiquesBinding
import com.example.mms.model.HourWeight
import com.example.mms.model.SpecificDaysHourWeight
import com.example.mms.service.WeekOfSpecificDaysService
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

class AddMedicamentJoursSpecifiquesFragment : Fragment() {
    private var _binding: FragmentAddMedicamentPlusJoursSpecifiquesBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SharedAMViewModel
    private lateinit var hourWeightAdapter: HourWeightAdapter
    private val weekOfSpecificDaysService = WeekOfSpecificDaysService()

    private lateinit var daysButtons: List<Button>
    private var selectedDay: Int = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[SharedAMViewModel::class.java]

        viewModel.setPreviousFragmentId(findNavController().currentDestination!!.id)

        _binding =
            FragmentAddMedicamentPlusJoursSpecifiquesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        this.daysButtons = listOf(
            binding.btnMon,
            binding.btnTue,
            binding.btnWed,
            binding.btnThu,
            binding.btnFri,
            binding.btnSat,
            binding.btnSun
        )

        for (numOfDay in 0..6) {
            // change the text of the button depending of the language
            val dayOfWeek = DayOfWeek.of(numOfDay + 1)
            this.daysButtons[numOfDay].text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

            // change the style
            if (numOfDay == this.selectedDay) {
                this.enableButton(numOfDay)
            } else {
                this.disableButton(numOfDay)
            }

            // add the listener
            this.daysButtons[numOfDay].setOnClickListener {
                this.disableButton(this.selectedDay)
                this.enableButton(numOfDay)
                this.selectedDay = numOfDay

                this.changeRecyclerViewDepingOnSelectedDay()
            }
        }

        // Create the adapter
        this.hourWeightAdapter = HourWeightAdapter(this.requireContext(), this.weekOfSpecificDaysService.getDay(this.selectedDay))
        val rvHeureDosage = binding.hourWeightListrv
        rvHeureDosage.adapter = this.hourWeightAdapter
        rvHeureDosage.layoutManager = LinearLayoutManager(this.requireContext())

        // Create the listener for the time picker
        val tvTimePickerOnClick = OnItemClickListener { position -> openTimePicker(position) }
        this.hourWeightAdapter.setOnItemClickListener(tvTimePickerOnClick)

        // Update the adapter
        this.changeRecyclerViewDepingOnSelectedDay()

        // buttons listeners
        binding.btnAddMedi.setOnClickListener {
            this.weekOfSpecificDaysService.addInDay(this.selectedDay, HourWeight(0, "08:00", 1))
            this.hourWeightAdapter.notifyDataSetChanged()
        }

        binding.backButton.root.setOnClickListener {
            viewModel.clearFrequencyData()
            goTo(requireActivity(), R.id.action_AMPlus_JoursSpecifiques_to_AMPlus_Fragment)
        }

        binding.nextButton.setOnClickListener {
            if (this.weekOfSpecificDaysService.getFlat().isEmpty()) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        this.requireContext(),
                        this.requireContext().getString(R.string.need_to_add_taking_in_list),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@setOnClickListener
            }

            val specificDays = mutableListOf<SpecificDaysHourWeight>()
            for (day in 0..6) {
                for (hourWeight in this.weekOfSpecificDaysService.getDay(day)) {
                    specificDays.add(SpecificDaysHourWeight(0, 0, day, hourWeight))
                }
            }

            viewModel.setSpecificDays(specificDays)
            goTo(requireActivity(), R.id.action_AMPlus_JoursSpecifiques_to_Storage)
        }

        return root
    }

    /**
     * Change the recycler view depending of the selected day
     */
    private fun changeRecyclerViewDepingOnSelectedDay() {
        this.hourWeightAdapter.hourWeightList = this.weekOfSpecificDaysService.getDay(this.selectedDay)
        this.hourWeightAdapter.notifyDataSetChanged()
    }

    /**
     * Switch the style of the button to enable it
     */
    private fun enableButton(numOfDay: Int) {
        this.daysButtons[numOfDay].setBackgroundResource(R.drawable.button_style_3)
        this.daysButtons[numOfDay].setTextColor(getColor(requireContext(), R.color.clickable_blue))
    }

    /**
     * Switch the style of the button to disable it
     */
    private fun disableButton(numOfDay: Int) {
        this.daysButtons[numOfDay].setBackgroundResource(R.drawable.button_style_3_disable)
        this.daysButtons[numOfDay].setTextColor(getColor(requireContext(), R.color.clickable_blue_disable))
    }

    /**
     * Open the time picker
     *
     * @param position the position of the item in the recycler view
     */
    private fun openTimePicker(position: Int) {
        val dialog = TimePickerDialog(
            this.requireContext(), { _, hourOfDay, minute ->
                val hourMinuteString = hourMinuteToString(hourOfDay, minute)
                this.weekOfSpecificDaysService.changeTime(this.selectedDay, position, hourMinuteString)
                this.hourWeightAdapter.notifyItemChanged(position)
            }, 8, 0, true
        )

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
