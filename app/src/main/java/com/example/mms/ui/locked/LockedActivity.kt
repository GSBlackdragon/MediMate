package com.example.mms.ui.locked

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.mms.MainActivity
import com.example.mms.R
import com.example.mms.Utils.cryptEmail
import com.example.mms.Utils.hashString
import com.example.mms.database.inApp.AppDatabase
import com.example.mms.database.inApp.SingletonDatabase
import com.example.mms.databinding.ActivityLockedBinding

class LockedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockedBinding
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var db: AppDatabase
    private lateinit var codePin: String

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityLockedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from intent
        val userEmail = intent.getStringExtra("userEmail")
        val isLinkedToBiometric = intent.getBooleanExtra("isLinkedToBiometric", false)

        db = SingletonDatabase.getDatabase(this)

        if (userEmail != null) { binding.userEmail.text = cryptEmail(userEmail) }else { binding.userEmail.isVisible = false }

        // Biometric
        val executor = ContextCompat.getMainExecutor(this)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // Gérer les erreurs d'authentification
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                // Authentification réussie, démarrer l'activité souhaitée par exemple
                Thread {
                    db.userDao().updateIsConnected(true, userEmail!!)
                    val intent = Intent(this@LockedActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }.start()

            }

            override fun onAuthenticationFailed() {
                // Gérer l'échec d'authentification
            }
        }
        biometricPrompt = BiometricPrompt(this, executor, callback)

        // Shuffle buttons
        var numbers = mutableListOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
        numbers = numbers.shuffled() as MutableList<Int>
        val listButtons = mutableListOf(
            binding.button0,
            binding.button1,
            binding.button2,
            binding.button3,
            binding.button4,
            binding.button5,
            binding.button6,
            binding.button7,
            binding.button8,
            binding.button9
        )
        for (i in 0 until numbers.size) {
            listButtons[i].text = numbers[i].toString()
        }

        // Set input type to number and max length to 1
        var currentIndex = 0
        val listCodePin = mutableListOf(
            binding.codePin1,
            binding.codePin2,
            binding.codePin3,
            binding.codePin4
        )
        listCodePin.forEach { it.inputType = InputType.TYPE_CLASS_NUMBER; it.filters = arrayOf<InputFilter>(
            InputFilter.LengthFilter(1))}

        fun codePin() {
            binding.buttonDelete.setOnClickListener {
                // Delete last number
                if (currentIndex > 0) {
                    currentIndex--
                    listCodePin[currentIndex].setText("")
                }
            }
            for (button in listButtons) {
                // Set onClickListener to each button
                button.setOnClickListener {
                    listCodePin[currentIndex].setText(button.text.toString())
                    currentIndex++
                    if (currentIndex == 4) {
                        currentIndex = 0
                        checkPassword(listCodePin, userEmail!!)
                    }
                }
            }
        }

        // if user is not linked to biometric, hide button
        val biometricManager = BiometricManager.from(this)
        if (!isLinkedToBiometric) {
            binding.buttonReturnToBiometrics.visibility = View.INVISIBLE
            binding.buttonReturnToBiometrics.isEnabled = false
            codePin()
        }
        else {

            // Check if biometric is available
            when (biometricManager.canAuthenticate()) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    // L'authentification est possible
                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle(getString(R.string.authentification_requise))
                        .setSubtitle(getString(R.string.authentifier_acceder_l_application))
                        .setNegativeButtonText(getString(R.string.annuler))
                        .build()
                    binding.buttonReturnToBiometrics.setOnClickListener {
                        biometricPrompt.authenticate(promptInfo)
                    }
                    biometricPrompt.authenticate(promptInfo)
                }
                else -> {
                    codePin()
                }
            }
        }
        binding.backButton.buttonArrowBack.setOnClickListener {
            finish()
        }

    }

    /**
     * Check if password is correct
     *
     * @param listCodePin list of EditText
     * @param userEmail user email
     */
    private fun checkPassword(listCodePin: MutableList<EditText>, userEmail: String) {

        Thread {
            val user = db.userDao().getUser(userEmail)
            val password = user!!.codePin
            val codePin =
                listCodePin[0].text.toString() + listCodePin[1].text.toString() + listCodePin[2].text.toString() + listCodePin[3].text.toString()
            if (hashString(codePin) == password) {
                db.userDao().updateIsConnected(true, userEmail)
                val intent = Intent(this@LockedActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            } else {
                listCodePin.forEach { it.setText("") }
                runOnUiThread {
                    Toast.makeText(this@LockedActivity, getString(R.string.code_pin_incorrect), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }.start()

    }
}
