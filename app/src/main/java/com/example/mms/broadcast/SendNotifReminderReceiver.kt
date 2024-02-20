package com.example.mms.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mms.model.Takes
import com.example.mms.service.NotifService
import com.example.mms.service.TasksService
import java.time.LocalDateTime

/**
 * Broadcast receiver for sending reminder notification.
 */
class SendNotifReminderReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        // check if context and intent are not null
        if (context == null || intent == null) {
            return
        }

        // get data from intent
        val medicineName = intent.getStringExtra("medicineName")
        val hourWeightId = intent.getIntExtra("hourWeightId", -1)
        val hasEnoughStock = intent.getBooleanExtra("hasEnoughStock", false)

        // check if data is not null
        if (medicineName == null || hourWeightId == -1) {
            return
        }

        // get actual date and initialize service
        val now = LocalDateTime.now()
        val tasksService = TasksService(context)

        // get the takes object
        var takes: Takes? = null
        val thread = Thread {
            takes = tasksService.getOrCreateTakes(hourWeightId, now)
        }
        thread.start()
        thread.join()

        // check if takes is already done, if so don't send notification
        if (takes!!.isDone) {
            return
        }

        // send notification
        val notifService = NotifService(context)
        notifService.sendReminder(medicineName, takes!!, hasEnoughStock)
    }
}
