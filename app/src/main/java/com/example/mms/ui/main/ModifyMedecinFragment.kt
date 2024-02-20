package com.example.mms.ui.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.example.mms.R
import com.example.mms.databinding.FragmentModifyMedecinBinding

class ModifyMedecinFragment : Fragment() {

    private var _binding: FragmentModifyMedecinBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentModifyMedecinBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        val sp = context?.getSharedPreferences("medecinInfo", Context.MODE_PRIVATE)

        binding.editNomMedecin.setText(sp?.getString("nom", ""))
        binding.editNumeroMedecin.setText(sp?.getString("numero", ""))
        binding.editMailMedecin.setText(sp?.getString("mail", ""))

        binding.backButton.root.setOnClickListener {
            navController.navigate(R.id.action_navigation_modify_medecin_to_navigation_notifications)
        }

        binding.buttonValiderMedecin.setOnClickListener {
            val nom = binding.editNomMedecin.text.toString()
            val numero = binding.editNumeroMedecin.text.toString()
            val mail = binding.editMailMedecin.text.toString()

            val editor = sp?.edit()
            editor?.putString("nom", nom)
            editor?.putString("numero", numero)
            editor?.putString("mail", mail)
            editor?.apply()

            navController.navigate(R.id.action_navigation_modify_medecin_to_navigation_notifications)
        }

        return root
    }
}