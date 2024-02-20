package com.example.mms.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mms.database.inApp.AppDatabase
import com.example.mms.database.inApp.SingletonDatabase
import com.example.mms.R
import com.example.mms.Utils.getAgeFromStringBirthDate
import com.example.mms.contrat.ModifiyAccountContrat
import com.example.mms.databinding.FragmentProfilBinding
import com.example.mms.service.TasksService
import com.example.mms.ui.loader.LoaderActivity
import com.example.mms.ui.modifyAccount.ModifyAccountActivity
import com.example.mms.ui.settings.SettingsActivity

class ProfilFragment : Fragment() {

    private var _binding: FragmentProfilBinding? = null
    private lateinit var db: AppDatabase
    private lateinit var viewModel: MainViewModel

    private val binding get() = _binding!!

    // We use this to get the result of the ModifyAccountActivity
    val modifyContrat : ActivityResultLauncher<Unit?> = registerForActivityResult(
        ModifiyAccountContrat()
    ) {
        if (it.name != "") {
            viewModel.setUserData(it)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        db = SingletonDatabase.getDatabase(requireContext())
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        val tasksService = TasksService(requireContext())
        val user = viewModel.userData.value

        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // We set an observer on the user data
        viewModel.userData.observe(viewLifecycleOwner) {
            binding.userName.text = it.surname
            binding.userNameProfil.text = it.surname
            binding.userAge.text =
                getAgeFromStringBirthDate(it.birthday).toString() + " " + resources.getString(
                    R.string.ans
                )
            binding.userSex.text = it.sexe
            binding.userWeight.text = "${it.weight} " + resources.getString(R.string.kg)
            binding.userHeight.text = "${it.height} " + resources.getString(R.string.cm)
        }

        // With room we have to launch a thread to get access to the database
        val tt = Thread {
            if (user != null) {
                // we get the user stats
                val usersStats = tasksService.getUserStats()
                val takesDone = usersStats.first
                val takesPercent = usersStats.second
                // We need to launch the UI thread to update the UI
                requireActivity().runOnUiThread {
                    // We set the progress bar and the text
                    binding.progressBarTakes.setProgress(takesPercent)
                    binding.percentTaken.text = takesPercent.toString() + "%"
                    binding.nbDoses.text = takesDone.toString() + " " + resources.getString(R.string.doses)
                    if(takesDone == 0) binding.felicitations.text = ""
                    if(takesPercent <= 50) {
                        binding.felicitations.text = resources.getString(R.string.des_efforts_sont_faire)
                    }else {
                        binding.felicitations.text = resources.getString(R.string.bravo_vous_tes_r_gulier)
                    }
                }
            }
        }
        tt.start()
        tt.join()

        // buttonLogout setOnClickListener
        binding.buttonLogout.setOnClickListener {
            Thread {
                // we update the user isConnected to false
                if (user != null) db.userDao().updateIsConnected(false, user.email)
                // we go to the loader activity and we clear the stack (we can't go back to the profil fragment)
                val intent = Intent(this.context, LoaderActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                requireActivity().finish()
            }.start()
        }


        binding.buttonParams.setOnClickListener {
            startActivity(Intent(root.context, SettingsActivity::class.java))
        }

        binding.buttonModifier.setOnClickListener {
            // we launch the contract to modify the account
            modifyContrat.launch(null)
        }

        binding.buttonLegalNotice.setOnClickListener {
            startActivity(Intent(root.context, LegalNoticeActivity::class.java))
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}