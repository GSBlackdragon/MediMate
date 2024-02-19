package com.example.mms.ui.modifyAccount

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.mms.database.inApp.AppDatabase
import com.example.mms.database.inApp.SingletonDatabase
import com.example.mms.R
import com.example.mms.constant.listAllergies
import com.example.mms.constant.listDietPlan
import com.example.mms.constant.listHealthDiseases
import com.example.mms.databinding.ActivityModifyAccountInformationBinding
import com.example.mms.ui.createAccount.Dialog.CustomDialogDiseasses
import com.example.mms.ui.main.MainViewModel
import com.example.mms.ui.main.ProfilFragment
import java.util.Calendar

class ModifyAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModifyAccountInformationBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityModifyAccountInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = SingletonDatabase.getDatabase(this)
        Thread {
            val user = db.userDao().getConnectedUser()!!
            this.runOnUiThread {
                // Set user information
                binding.editNom.setText(user.name)
                binding.editPrenom.setText(user.surname)
                binding.editTaille.setText(user.height.toString())
                binding.editPoids.setText(user.weight.toString())
                binding.editBirthdate.setText(user.birthday)
                binding.editAllergies.setText(user.listAllergies)

                binding.editPoids.inputType = InputType.TYPE_CLASS_NUMBER
                binding.editPoids.filters = arrayOf(InputFilter.LengthFilter(3))
                binding.editTaille.inputType = InputType.TYPE_CLASS_NUMBER
                binding.editTaille.filters =  arrayOf(InputFilter.LengthFilter(3))

                // Partie soucis de santé / allergies / régime non implémentée
                /*
                binding.buttonAddHealthDisease.setOnClickListener {
                    val selectedHealthDiseases = user.listHealthDiseases?.split(",")!!.toList()
                    val dialog = CustomDialogDiseasses(this, listHealthDiseases, selectedHealthDiseases) {
                        it.forEachIndexed { index, healthDisease ->
                            user.listHealthDiseases += if (index < it.size - 1) { "$healthDisease," } else { healthDisease }
                        }
                        binding.editSoucis.text = getString(R.string.elements_selectionnes, it.size.toString())
                    }
                    dialog.show()

                    dialog.setOnDismissListener {
                        val updatedSelectedAllergies = user.listAllergies.split(",").toList()
                        dialog.updateSelectedItems(updatedSelectedAllergies)
                    }
                }

                 */
                binding.buttonAddAllergies.setOnClickListener {
                    val selectedAllergies = user.listAllergies.split(",").toList().map { it.trim() }
                    val dialog = CustomDialogDiseasses(this, listAllergies, selectedAllergies) {
                        user.listAllergies=""
                        for (element in it) {
                            user.listAllergies += if (element==it.last()){
                                element
                            }else{
                                "$element, "
                            }
                        }
                        binding.editAllergies.text = user.listAllergies
                    }
                    dialog.show()

                    dialog.setOnDismissListener {
                        val updatedSelectedAllergies = user.listAllergies.split(",").toList()
                        dialog.updateSelectedItems(updatedSelectedAllergies)
                    }
                }
                /*
                binding.buttonAddDietPlan.setOnClickListener {
                    val selectedDietPlan = user.listDietPlan.split(",").toList()
                    val dialog = CustomDialogDiseasses(this, listDietPlan, selectedDietPlan) {
                        it.forEachIndexed { index, dietPlan ->
                            user.listDietPlan += if (index < it.size - 1) { "$dietPlan," } else { dietPlan }
                        }
                        binding.editRegime.text = getString(R.string.elements_selectionnes, it.size.toString())
                    }
                    dialog.show()

                    dialog.setOnDismissListener {
                        val updatedSelectedAllergies = user.listDietPlan.split(",").toList()
                        dialog.updateSelectedItems(updatedSelectedAllergies)
                    }
                }

                 */

                binding.editBirthdate.keyListener = null
                binding.editBirthdate.isFocusable = false

                // listener to edit birthdate
                binding.editBirthdate.setOnClickListener {
                    // init calendar
                    val calendar = Calendar.getInstance()
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)

                    // create date picker dialog
                    val datePickerDialog = DatePickerDialog(
                        this,
                        DatePickerDialog.OnDateSetListener { view, selectedYear, selectedMonth, selectedDay ->
                            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                            binding.editBirthdate.setText(selectedDate)
                        },
                        year,
                        month,
                        day
                    )
                    // set max date to today
                    datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

                    datePickerDialog.show()
                }

                // filter to edit name and surname
                val nameSurnameRegex = Regex("^[a-zA-ZÀ-ÿ-'\\s]+$")
                val nameSurnameFilter = InputFilter { source, start, end, dest, dstart, dend ->
                    if (source != null && !source.toString().matches(nameSurnameRegex)) {
                        ""  // Si le texte ne correspond pas à la regex, le caractère est supprimé
                    } else {
                        null // Sinon, le caractère est accepté
                    }
                }

                // set filters
                binding.editNom.filters = arrayOf(nameSurnameFilter)
                binding.editPrenom.filters = arrayOf(nameSurnameFilter)

                // validate modification
                binding.btnModifyValidate.setOnClickListener {
                    // get user information
                    val name = binding.editNom.text.toString()
                    val surname = binding.editPrenom.text.toString()
                    val brithDate = binding.editBirthdate.text.toString()
                    val weightText = binding.editPoids.text.toString()
                    val heightText = binding.editTaille.text.toString()

                    val nameRegex = Regex("^[a-zA-ZÀ-ÿ\\s]+$")

                    // check if all fields are correct
                    if (name.matches(nameRegex) &&
                        surname.matches(nameRegex) &&
                        brithDate.isNotBlank()
                    ) {
                        val weight = weightText.toInt()
                        val height = heightText.toInt()

                        user.name = name
                        user.surname = surname
                        user.birthday = brithDate
                        user.weight = weight
                        user.height = height

                        // update user
                        Thread{
                            db.userDao().updateUser(user)
                        }.start()

                        // return to profil fragment
                        val intent = Intent().putExtra("user", user)
                        setResult(RESULT_OK, intent)
                        finish()
                    } else {
                        Toast.makeText(this, R.string.check_fields, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.start()

        binding.backButton.buttonArrowBack.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

    }

}