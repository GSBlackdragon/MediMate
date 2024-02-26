package com.example.mms.ui.loader

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.mms.R
import com.example.mms.broadcast.MidnightAlarmReceiver
import com.example.mms.database.inApp.AppDatabase
import com.example.mms.database.inApp.SingletonDatabase
import com.example.mms.database.jsonMedicines.MedicineJsonDatabase
import com.example.mms.databinding.LoaderBinding
import com.example.mms.model.SideInfoMedicine
import com.example.mms.model.User
import com.example.mms.service.NotifService
import com.example.mms.service.TasksService
import com.example.mms.service.UpdateDataService
import com.example.mms.ui.login.LoginActivity
import com.example.mms.ui.welcome.WelcomeActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.util.Calendar

class LoaderActivity : AppCompatActivity() {
    private lateinit var binding: LoaderBinding
    private lateinit var db: AppDatabase
    private lateinit var updateDataService: UpdateDataService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = LoaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textLoading.text = getString(R.string.chargement)

        db = SingletonDatabase.getDatabase(this)

        // Set the midnight alarm if it's not already set
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!this.isMidnightAlarmSet()) {
                this.startMidnightAlarmManager()
            }
        }

        // == Update the medicines database ==
        updateDataService = UpdateDataService(this)

        val t =Thread {
            val mediDB = db.medicineDao()

            if (mediDB.getNbElements() == 0) {
                val mediJsDb = MedicineJsonDatabase(this)
                mediJsDb.transferJsonDBintoRoom(db.medicineDao())
            }

            // call the api to check if the local version is up to date
            this.updateDataService.needToUpdate(
                { needToUpdate ->
                    if (!needToUpdate) {
                        this.showSnackbar()
                        this.nextPage()
                        return@needToUpdate
                    }

                    Log.i("update", "new version available")

                    runOnUiThread {
                        binding.downloadText.visibility = View.VISIBLE
                        binding.downloadProgressBar.visibility = View.VISIBLE
                        binding.textLoading.text = getString(R.string.maj_donnees)
                        binding.progressBar.visibility = View.INVISIBLE
                    }

                    Thread {
                        val numElements = this.updateDataService.numberOfItemsToDownload()
                        var progress: Int

                        // Download the medicines
                        fun downloadNext() {
                            this.updateDataService.nextDownload({
                                progress = ((this.updateDataService.nbMedicinesToDownload.toDouble() * 100) / numElements.toDouble()).toInt()

                                runOnUiThread {
                                    binding.downloadProgressBar.progress = progress
                                    binding.downloadText.text = "$progress%"
                                }

                                if (this.updateDataService.isNotFinish()) {
                                    downloadNext()
                                } else {
                                    this.updateDataService.updateLocalVersion()
                                    this.nextPage()
                                }
                            }, {
                                this.showSnackbar()
                                this.nextPage()
                            })
                        }
                        downloadNext()
                    }.start()
                },
                {
                    this.showSnackbar()
                    this.nextPage()
                })
        }
        t.start()
        t.join()
        var t2 = Thread{
            @Serializable
            data class SideInfo(
                val Warning: List<String>,
                val Allergie: List<String>,
                val Content: String
            )

            var map = Json.decodeFromString<Map<String, SideInfo>>(this.assets.open("databases/sideEffect.json").bufferedReader().use {
            it.readText()
        })
            var storage = mutableListOf<SideInfoMedicine>()
            map.forEach {
                storage.add(SideInfoMedicine(it.key, it.value.Warning.toString().replace("[","").replace("]",""), it.value.Allergie.toString().replace("[","").replace("]",""), it.value.Content,""))
            }
            Thread{
                db.sideInfoMedicineDao().insertMany(storage)
            }.start()

        }
        t2.start()
        t2.join()

        Thread{

            var map = Json.decodeFromString<Map<String, List<Int>>>(this.assets.open("databases/super.json").bufferedReader().use {
                it.readText()
            })

            Thread{
                map.forEach{

                    db.sideInfoMedicineDao().addBadInteraction(it.key, it.value.toString().replace("[","").replace("]",""))
                }
            }.start()
        }.start()



    }

    private fun nextPage() {
        var users = listOf<User>()
        val t = Thread {
            users = db.userDao().getAllUsers()
        }
        t.start()
        t.join()

        if (users.isEmpty()) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    /**
     * Show a snackbar to inform the user that the connection failed
     */
    private fun showSnackbar() {
        val snackbar = Snackbar.make(
            binding.root,
            getString(R.string.connexion_echouee),
            Snackbar.LENGTH_INDEFINITE
        )

        snackbar.setBackgroundTint(getColor(R.color.white))
        snackbar.setTextColor(getColor(R.color.black))

        // Position the snackbar on top
        val view = snackbar.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        params.topMargin = 100
        view.layoutParams = params

        snackbar.show()
    }

    /**
     * Set the midnight alarm to planify the notifications
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun startMidnightAlarmManager() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1) // Add one day to be sure that the alarm will be triggered tomorrow
        }

        val intent = Intent(this, MidnightAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)


        // Set the alarm at midnight and repeat it every day
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        // Planify the notifications for today
        this.planifyTodayTakesNotifications()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun planifyTodayTakesNotifications() {
        // Get the hour weights for all users
        val tasksService = TasksService(this)
        var todaysShowableHourWeights = tasksService.getTodaysHourWeightsForAllUsers()
        todaysShowableHourWeights = tasksService.removeAlreadyPassedHourWeights(todaysShowableHourWeights)

        // Planify the notifications
        val notifService = NotifService(this)
        notifService.planifyTakesNotifications(todaysShowableHourWeights)
    }

    private fun isMidnightAlarmSet(): Boolean {
        val intent = Intent(this, MidnightAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_MUTABLE)
        return pendingIntent != null
    }
}
