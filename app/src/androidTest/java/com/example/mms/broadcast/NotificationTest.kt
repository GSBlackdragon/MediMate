package com.example.mms.broadcast

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import androidx.test.core.app.ApplicationProvider
import com.example.mms.database.inApp.SingletonDatabase
import com.example.mms.model.HourWeight
import com.example.mms.model.MedicineStorage
import com.example.mms.model.ShowableHourWeight
import com.example.mms.model.Task
import com.example.mms.model.medicines.MType
import com.example.mms.service.NotifService
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class NotificationTest {

    private var context: Context = ApplicationProvider.getApplicationContext()
    private var intent: Intent = Intent(context, SendNotifReminderReceiver::class.java)
    private val db = SingletonDatabase.getDatabase(context)

    private var notifService: NotifService = NotifService(context)

    init {
        try {
            this.db.hourWeightDao().insert(
                HourWeight(
                    id = 1,
                    hour = "12:00",
                    weight = 1
                )
            )
        } catch (e: SQLiteConstraintException) {
            // Si l'hourWeight existe déjà, on ne fait rien
        }
    }


    @Before
    fun setUp() {
        this.intent = Intent(context, SendNotifReminderReceiver::class.java)
    }


    // == Send Notification ==
    @Test
    fun testOnNoExtraReceive() {
        val currentNotifId = notifService.getNextNotificationId()

        val receiver = SendNotifReminderReceiver()
        receiver.onReceive(context, intent)

        // Vérifie que le prochain id est + 1 par rapport à l'ancien
        // car la notification ne s'est pas envoyé
        val newNotifId = notifService.getNextNotificationId()
        assert(currentNotifId + 1 == newNotifId)
    }

    @Test
    fun testOnReceive() {
        val currentNotifId = notifService.getNextNotificationId()

        this.intent.putExtra("medicineName", "Doliprane")
        this.intent.putExtra("hourWeightId", 1)
        this.intent.putExtra("hasEnoughStock", true)

        val receiver = SendNotifReminderReceiver()
        receiver.onReceive(context, intent)

        // Vérifie que le prochain id est + 2 par rapport à l'ancien
        // car la notification s'est envoyé et a donc pris l'id entre les 2
        val newNotifId = notifService.getNextNotificationId()
        assert(currentNotifId + 2 == newNotifId)
    }


    // == Planify Notification ==
    @Test
    fun planifyOneNotificationFailTest() {
        // Création d'un ShowableHourWeight
        val showableHourWeight = ShowableHourWeight(
            medicineName = "Doliprane",
            medicineType = MType(),
            hourWeight = HourWeight(
                id = 1,
                hour = "12:00",
                weight = 1
            ),
            task = Task(),
            medicineStorage = MedicineStorage(1, 10, 1)
        )

        // utilisation de mockk pour simuler l'heure actuelle
        // pour tester que si l'heure de la notification est passée, on ne planifie pas la notification
        val localDateTimeMock = mockk<LocalDateTime>()
        every { localDateTimeMock.hour } returns 18
        every { localDateTimeMock.minute } returns 20

        val notificationIsPlanned = notifService.planningOneNotification(showableHourWeight, localDateTimeMock)
        assert(!notificationIsPlanned)
    }

    @Test
    fun planifyOneNotificationTest() {
        // Création d'un ShowableHourWeight
        val showableHourWeight = ShowableHourWeight(
            medicineName = "Doliprane",
            medicineType = MType(),
            hourWeight = HourWeight(
                id = 1,
                hour = "12:00",
                weight = 1
            ),
            task = Task(),
            medicineStorage = MedicineStorage(1, 10, 1)
        )
        val now = LocalDateTime.now()

        // utilisation de mockk pour simuler l'heure actuelle
        // pour tester que si l'heure de la notification est pas encore passée, on planifie la notification
        val localDateTimeMock = mockk<LocalDateTime>()
        every { localDateTimeMock.hour } returns 2
        every { localDateTimeMock.minute } returns 24

        // retourne un vrai LocalDateTimen pour que la suite de l'exécution de la fonction planifyOneNotification
        // ne plante pas
        every { localDateTimeMock.withHour(any()) } returns LocalDateTime.of(
            now.year,
            now.month,
            now.dayOfMonth,
            localDateTimeMock.hour,
            localDateTimeMock.minute
        )

        val notificationIsPlanned = notifService.planningOneNotification(showableHourWeight, localDateTimeMock)
        assert(notificationIsPlanned)
    }
}
