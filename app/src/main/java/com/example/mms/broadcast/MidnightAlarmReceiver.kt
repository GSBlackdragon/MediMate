package com.example.mms.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.mms.service.MedicineStorageService
import com.example.mms.service.NotifService
import com.example.mms.service.TasksService
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
        notifService.planningTakesNotifications(todaysShowableHourWeights)

        // Update end date of "Stop on stock" treatments
        val medicineStorageService = MedicineStorageService(context,null)
        val listTaskStopOnStock = tasksService.getCurrentUserTasks().filter { it.stopOnStock }

        for (task in listTaskStopOnStock){
            val totalStock = medicineStorageService.getMedicineStorageByMedicineId(task.medicineCIS)?.storage
            val totalWeight = task.cycle.hourWeights.sumOf { it.weight }
            val totalDaysToAdd = totalStock?.div(totalWeight)

            task.endDate= LocalDateTime.now().plusDays(totalDaysToAdd!!.toLong()-1)
            tasksService.updateTask(task)
        }

    }
}
