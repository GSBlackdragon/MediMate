package com.example.mms.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.mms.R
import com.example.mms.Utils.dateToString
import com.example.mms.Utils.stringHourMinuteToInt
import com.example.mms.broadcast.NotifTakesButtonReceiver
import com.example.mms.broadcast.SendNotifReminderReceiver
import com.example.mms.model.ShowableHourWeight
import com.example.mms.model.Takes
import com.example.mms.ui.loader.LoaderActivity
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

/**
 * Service to send or planify notifications
 * Singleton pattern
 *
 * @property context The context of the application
 * @property CHANNEL_MEDI_REMINDER The channel id of reminder notifications
 * @property KEY_NOTIFICATION_ID The key of the notification id in the shared preferences
 * @property PREFS_NAME The name of the shared preferences
 * @property sharedPref The shared preferences of the application
 *
 */
class NotifService(
    private val context: Context
) {
    private val CHANNEL_MEDI_REMINDER = "TAKE_MEDICINE_REMINDER"

    private val PREFS_NAME = "notification_prefs"
    private val KEY_NOTIFICATION_ID = "notification_id"
    private val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)


    /**
     * Create a notification channel
     */
    private fun createNotificationChannel() {
        // define the channel's name and description
        val name = this.context.getString(R.string.channel_name_reminder)
        val descriptionText = this.context.getString(R.string.channel_description_reminder)

        // define the importance level of the notification
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(this.CHANNEL_MEDI_REMINDER, name, importance).apply {
            description = descriptionText
        }

        // Register the channel with the system.
        val notificationManager: NotificationManager =
            this.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Send a notification to remind the user to take his medicine
     *
     * @param medicineName The name of the medicine
     * @param takes The takes of the medicine
     * @param hasEnoughStock True if the user has enough stock to take the medicine
     */
    fun sendReminder(medicineName: String, takes: Takes, hasEnoughStock: Boolean) {
        val message = this.context.getString(R.string.notification_message)

        this.createNotificationChannel()

        val notifId = this.getNextNotificationId()

        // Create an intent to open the app when the user clicks on the notification
        val intent = Intent(this.context, LoaderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this.context, 3, intent, PendingIntent.FLAG_IMMUTABLE)

        // Build the notification
        val builder = NotificationCompat.Builder(this.context, this.CHANNEL_MEDI_REMINDER)
            .setSmallIcon(R.drawable.medicament)
            .setContentTitle(medicineName)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        // If enough stock, we can take on the notification
        if (hasEnoughStock) {
            // Get the date of the takes
            val dateString = dateToString(takes.date.dayOfMonth, takes.date.monthValue, takes.date.year)

            // Create an intent to take the medicine when the user clicks on the notification
            val takeMedicineIntent = Intent(this.context, NotifTakesButtonReceiver::class.java).apply {
                putExtra("notifId", notifId)
                putExtra("hourWeightId", takes.hourWeightId)
                putExtra("date", dateString)
            }

            // Add the action to the notification
            val takeMedicinePendingIntent = PendingIntent.getBroadcast(this.context, notifId, takeMedicineIntent, PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(
                R.drawable.medicament,
                this.context.getString(R.string.pris),
                takeMedicinePendingIntent
            )
        }

        // Get an instance of NotificationManager
        val notificationManager: NotificationManager =
            this.context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Notify the notification
        notificationManager.notify(notifId, builder.build())
    }

    /**
     * Planify the given notifications
     *
     * @param showableHourWeights The list of notifications to planify
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun planningTakesNotifications(showableHourWeights: List<ShowableHourWeight>) {
        for (hourWeight in showableHourWeights) {
            this.planningOneNotification(hourWeight)
        }
    }

    /**
     * Planify a reminder notification
     *
     * @param showableHourWeight The notification to planify
     * @param now The current date, this parameter is used for testing
     *
     * @return True if the notification has been planified, false otherwise
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun planningOneNotification(showableHourWeight: ShowableHourWeight, now: LocalDateTime = LocalDateTime.now()): Boolean {
        val alarmManager = this.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val hourMin = stringHourMinuteToInt(showableHourWeight.hourWeight.hour)

        // if the hour is passed, we don't planify the notification
        if (now.hour > hourMin.first || (now.hour == hourMin.first && now.minute > hourMin.second)) {
            return false
        }

        // Create a LocalDateTime with the hour and minute of the notification
        val takesDate = now
            .withHour(hourMin.first)
            .withMinute(hourMin.second)
            .withSecond(0)
        val zonedDateTime = ZonedDateTime.of(takesDate, ZoneOffset.systemDefault())

        // Create an intent to send the notification
        val intent = Intent(this.context, SendNotifReminderReceiver::class.java).apply {
            putExtra("medicineName", showableHourWeight.medicineName)
            putExtra("hourWeightId", showableHourWeight.hourWeight.id)
            putExtra("hasEnoughStock", showableHourWeight.hasEnoughStock())
        }
        val pendingIntent = PendingIntent.getBroadcast(this.context, showableHourWeight.hourWeight.id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        // if the device can schedule exact alarms, we planify the notification
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                zonedDateTime.toEpochSecond() * 1000, // convert to milliseconds
                pendingIntent
            )
            return true
        }
        return false
    }

    /**
     * Get the next free notification id
     * The notification id is stored in the shared preferences
     *
     * @return The next notification id
     */
    fun getNextNotificationId(): Int {
        // Get the current notification ID
        val prevNotificationId = sharedPref.getInt(KEY_NOTIFICATION_ID, 3)
        val notificationId = prevNotificationId + 1

        // Increment the notification ID and store it
        with(sharedPref.edit()) {
            putInt(KEY_NOTIFICATION_ID, notificationId)
            apply()
        }

        return notificationId
    }
}
