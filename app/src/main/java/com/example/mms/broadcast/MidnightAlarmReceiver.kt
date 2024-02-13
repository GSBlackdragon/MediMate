package com.example.mms.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.mms.service.MedicineStorageService
import com.example.mms.service.NotifService
import com.example.mms.service.TasksService
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Receiver triggered at midnight to planify daily notifications.
 */
class MidnightAlarmReceiver: BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context?, intent: Intent?) {
        // Check if the context is null
        if (context == null) {
            return
        }

        // Get the hour weights for all users
        val tasksService = TasksService(context)
        val todaysShowableHourWeights = tasksService.getTodaysHourWeightsForAllUsers()

        // Planify the notifications
        val notifService = NotifService(context)
        notifService.planifyTakesNotifications(todaysShowableHourWeights)

        // Update end date of "Stop on stock" treatments
        val medicineStorageService = MedicineStorageService(context,null)
        var listTaskStopOnStock = tasksService.getCurrentUserTasks().filter { it.stopOnStock }

        for (task in listTaskStopOnStock){
            var totalStock = medicineStorageService.getMedicineStorageByMedicineId(task.medicineCIS)?.storage
            var totalWeight = task.cycle.hourWeights.map { it.weight }.sum()
            var totalDaysToAdd = totalStock?.div(totalWeight)

            task.endDate= LocalDateTime.now().plusDays(totalDaysToAdd!!.toLong()-1)
            tasksService.updateTask(task)
        }

    }
}
