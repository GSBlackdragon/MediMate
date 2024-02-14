package com.example.mms.ui.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mms.R
import com.example.mms.Utils.goTo
import com.example.mms.databinding.FragmentAddMedicamentPlusOptionBinding

class AddMedicamentPlusOptionFragment : Fragment() {
    private var _binding: FragmentAddMedicamentPlusOptionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel = ViewModelProvider(requireActivity())[SharedAMViewModel::class.java]

        viewModel.setPreviousFragmentId(findNavController().currentDestination!!.id)

        _binding = FragmentAddMedicamentPlusOptionBinding.inflate(inflater, container, false)
        val root: View = binding.root


        binding.backButton.buttonArrowBack.setOnClickListener {
            goTo(requireActivity(), R.id.action_AMPlus_Fragment_to_AM2_Fragment)
        }

        binding.nextButton.setOnClickListener {
            val nextLayoutId = this.getLayoutWithCheckedRadio()
            goTo(requireActivity(),nextLayoutId)
        }

        return root
    }

    private fun getLayoutWithCheckedRadio(): Int {
        return when (true) {
            binding.radioIntervalle.isChecked -> R.id.action_AMPlus_Fragment_to_AMPlusIntervalle_Fragment
            binding.radioSpecificDay.isChecked -> R.id.action_navigation_AMPlus_to_AMJoursSpecifiques_Fragement
            binding.radioCycle.isChecked -> R.id.action_navigation_AMPlus_to_AMCycle
            else -> {
                R.id.action_AMPlus_Fragment_to_AM2_Fragment
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
