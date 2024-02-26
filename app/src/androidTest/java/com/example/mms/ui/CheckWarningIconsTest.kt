package com.example.mms.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.mms.database.inApp.SingletonDatabase
import com.example.mms.service.TasksService

class CheckWarningIconsTest {


    private var context: Context = ApplicationProvider.getApplicationContext()
    private val db = SingletonDatabase.getDatabase(context)
    private val tasksService = TasksService(context)
    /*
    @Before
    fun setup() {
        db.userDao().deleteAllUsers()
        tasksService.deleteAllTasks()
        println(db.userDao().getAllUsers())
        val user = User(
            "test",
            "test",
            "test@test.fr",
            "test",
            "test",
            0,
            0,
            true,
            "test",
            "test",
            "test",
            "test",
            false
        )
        try {
            db.userDao().insertUser(user)
        } catch (e: Exception) {
            // Si l'utilisateur existe déjà, on ne fait rien
        }
    }

    @Test
    fun checkWarningIcons() {
        //Ajout du User et création de la task
        var userConnected: User? = null
        try {
            userConnected = db.userDao().getConnectedUser()
        } catch (e: Exception) {
            throw Exception("No user connected")
        }
        println(userConnected)
        //Test type de forme de médicament
        var tasks = listOf(
            //Comprimé
            Task(
                0, "", LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                LocalDateTime.now(), false, 61989835, userConnected!!.email, Cycle(), mutableListOf()
            ),
            //Suppositoire
            Task(
                1, "", LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                LocalDateTime.now(), false, 60916357, userConnected!!.email, Cycle(), mutableListOf()
            ),
            //Gelule
            Task(
                2, "", LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                LocalDateTime.now(), false, 69663414, userConnected!!.email, Cycle(), mutableListOf()
            ),
            //Solution Buvable
            Task(
                3, "", LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                LocalDateTime.now(), false, 63426508, userConnected!!.email, Cycle(), mutableListOf()
            ),
            //Suspension (pipette)
            Task(
                4, "", LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                LocalDateTime.now(), false, 61693569, userConnected!!.email, Cycle(), mutableListOf()
            )
        )
        println(tasks.first().userId)
        println(userConnected.email)

        for (task in tasks){
            try {
                tasksService.storeTask(task)
            } catch (e: Exception) {
                throw Exception(e.toString())
            }
        }


        var items=tasksService.createShowableHourWeightsFromTasks(tasks)

        assert(items[0].medicineType.generic=="comprime")

        assert(items[1].medicineType.generic=="suppositoire")

        assert(items[2].medicineType.generic=="gelule")

        assert(items[3].medicineType.generic=="solution buvable")

        assert(items[4].medicineType.generic=="suspension")

    }

    @After
    fun teardown() {
        // Fermer la base de données
        db.close()
    }

     */
}