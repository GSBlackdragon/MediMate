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
import com.example.mms.adapter.HourWeightAdapter
import com.example.mms.adapter.Interface.OnItemClickListener
import com.example.mms.R
import com.example.mms.Utils.goTo
import com.example.mms.Utils.hourMinuteToString
import com.example.mms.databinding.FragmentAddMedicamentUnPlusieursParJourBinding
import com.example.mms.model.Cycle
import com.example.mms.model.HourWeight

class AddMedicamentUnOuPlusParJourFragment : Fragment() {
    private var _binding: FragmentAddMedicamentUnPlusieursParJourBinding? = null
    private lateinit var hourWeightAdapter: HourWeightAdapter
    private val binding get() = _binding!!
    private var hourWeightList: MutableList<HourWeight> = mutableListOf()
    private lateinit var viewModel: SharedAMViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[SharedAMViewModel::class.java]

        viewModel.setPreviousFragmentId(findNavController().currentDestination!!.id)

        _binding =
            FragmentAddMedicamentUnPlusieursParJourBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.backButton.root.setOnClickListener {
            viewModel.clearFrequencyData()
            goTo(requireActivity(),R.id.action_AMUnOuPlusParJour_Fragment_to_AMPlus_Fragment)
        }

        if (this.hourWeightList.isEmpty()){
            hourWeightList.add(HourWeight(0, "08:00", 1))
        }


        // create an adapter to display the list of hourWeight
        this.hourWeightAdapter = HourWeightAdapter(this.requireContext(), hourWeightList)
        val rvHeureDosage = binding.recyclerView
        rvHeureDosage.adapter = this.hourWeightAdapter
        rvHeureDosage.layoutManager = LinearLayoutManager(this.requireContext())

        binding.btnAddMedi.setOnClickListener {
            this.hourWeightList.add(HourWeight(0, "08:00", 1))
            this.hourWeightAdapter.notifyDataSetChanged()
        }


        // set the onClickListener for the timePicker
        val tvTimePickerOnClick = object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                openTimePicker(position)
            }
        }
        this.hourWeightAdapter.setOnItemClickListener(tvTimePickerOnClick)

        binding.nextButton.setOnClickListener {
            // check if the list is empty
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

            // store the data in the viewModel
            val cycle = Cycle(0,0,24,0,0, hourWeightList)
            viewModel.setCycle(cycle)

            goTo(requireActivity(),R.id.action_AMUnOuPlusParJour_Fragment_to_storage)
        }

        return root
    }

    /**
     * Open a timePickerDialog and set the hourWeightList[position].hour to the selected time
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
