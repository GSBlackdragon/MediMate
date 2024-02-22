package com.example.mms.ui

import android.content.Context
import android.os.Parcel
import android.util.Log
import android.view.View
import androidx.annotation.IdRes
import androidx.media3.common.util.Assertions
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.example.mms.MainActivity
import com.example.mms.database.inApp.SingletonDatabase
import com.example.mms.model.Cycle
import com.example.mms.model.HourWeight
import com.example.mms.service.TasksService
import com.example.mms.model.Task
import com.example.mms.model.User
import org.junit.Test
import com.example.mms.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matcher
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.After
import org.junit.Before
import java.time.LocalDateTime

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