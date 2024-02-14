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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mms.R
import com.example.mms.Utils.goTo
import com.example.mms.Utils.hourMinuteToString
import com.example.mms.adapter.HourWeightAdapter
import com.example.mms.adapter.Interface.OnItemClickListener
import com.example.mms.databinding.FragmentAddMedicamentCycleHourweightBinding
import com.example.mms.model.HourWeight

class AddMedicamentCycleHourWeightFragment: Fragment() {
    private var _binding: FragmentAddMedicamentCycleHourweightBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SharedAMViewModel
    private lateinit var hourWeightAdapter: HourWeightAdapter
    private val hourWeightList = mutableListOf<HourWeight>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[SharedAMViewModel::class.java]

        viewModel.setPreviousFragmentId(findNavController().currentDestination!!.id)

        _binding = FragmentAddMedicamentCycleHourweightBinding.inflate(inflater, container, false)

        val root: View = binding.root

        if (this.hourWeightList.isEmpty()){
            this.hourWeightList.add(HourWeight(0, "08:00", 1))
        }


        // Creation of the adapter
        this.hourWeightAdapter = HourWeightAdapter(this.requireContext(), this.hourWeightList)
        val recyclerView = binding.hourWeightRecyclerView
        recyclerView.adapter = this.hourWeightAdapter
        recyclerView.layoutManager = LinearLayoutManager(this.requireContext())

        // OnClick listener for the time picker
        val tvTimePickerOnClick = object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                openTimePicker(position)
            }
        }
        this.hourWeightAdapter.setOnItemClickListener(tvTimePickerOnClick)

        // buttons listeners
        binding.backButton.buttonArrowBack.setOnClickListener {
            goTo(requireActivity(), R.id.action_AMCycleHourWeight_to_AMCycle)
        }

        binding.btnAddMedi.setOnClickListener {
            this.hourWeightList.add(HourWeight(0, "08:00", 1))
            this.hourWeightAdapter.notifyItemInserted(this.hourWeightList.size - 1)
        }

        binding.nextButton.setOnClickListener {
            if (this.hourWeightList.size == 0) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        this.requireContext(),
                        this.requireContext().getString(R.string.need_to_add_taking_in_list),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@setOnClickListener
            }

            val cycle = viewModel.cycle.value!!
            cycle.hourWeights = this.hourWeightList
            viewModel.setCycle(cycle)

            goTo(requireActivity(), R.id.action_AMCycleHourWeight_to_Storage)
        }

        return root
    }

    /**
     * Open a time picker dialog and set the time into a item
     */
    private fun openTimePicker(position: Int) {
        val dialog = TimePickerDialog(
            this.requireContext(), { _, hourOfDay, minute ->
                this.hourWeightList[position].hour = hourMinuteToString(hourOfDay, minute)
                this.hourWeightAdapter.notifyItemChanged(position)
            }, 8, 0, true
        )

        dialog.show()
    }
}
