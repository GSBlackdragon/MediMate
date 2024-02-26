package com.example.mms.ui

import android.content.Context
import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
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
import com.example.mms.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class RecyclerViewContentTest {

    private var context: Context = ApplicationProvider.getApplicationContext()
    private val db = SingletonDatabase.getDatabase(context)

    private val tasksService = TasksService(context)
    @Test
    fun addUser() {
        val user = User("test", "test", "test@test.fr", "test", "test", 0, 0, false, "test", "test", "test", "test", false)
        try {
            db.userDao().insertUser(user)
        }catch (e: Exception){
            // Si l'utilisateur existe déjà, on ne fait rien
        }
    }

    @Test
    fun checkRecyclerViewContent(){
        addUser()
        var userConnected : User? = null
        try {
            userConnected = db.userDao().getConnectedUser()
        }catch (e: Exception){
            throw Exception("No user connected")
        }
        var task = Task(0, "", LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
            LocalDateTime.now(), false,66057393, userConnected!!.email, Cycle(), mutableListOf()
        )
        try {
            tasksService.storeTask(task)
            task = db.taskDao().getLastInserted()!!
        }catch (e: Exception){
           throw Exception(e.toString())
        }

        val cycle = Cycle(0, task.id, 24, 0, 0, mutableListOf(
            HourWeight(0, "12:00", 1),
        ))

        try {
            tasksService.storeCycle(cycle)
        }catch (e: Exception){
           throw Exception(e.toString())
        }



        // Test if the task is in the recycler view

        ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.nav_view)).perform(navigateTo(R.id.navigation_home))

        onView(withId(R.id.medicRecyclerView)).check(matches(isDisplayed()))

        onView(withId(R.id.medicRecyclerView)).check(RecyclerViewItemCountAssertion.getItemCount(1))

    }

    fun navigateTo(@IdRes menuItemId: Int): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return allOf(
                    isDisplayed(),
                    withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                    isAssignableFrom(BottomNavigationView::class.java)
                )
            }

            override fun getDescription(): String {
                return "Click on menu item with id: $menuItemId"
            }

            override fun perform(uiController: UiController, view: View) {
                val navigationView = view as BottomNavigationView
                navigationView.selectedItemId = menuItemId
            }
        }
    }

    class RecyclerViewItemCountAssertion(private val expectedCount: Int) : ViewAssertion {
        override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
            if (noViewFoundException != null) {
                throw noViewFoundException
            }

            val recyclerView = view as RecyclerView
            val adapter = recyclerView.adapter
            assertThat("RecyclerView adapter item count", adapter?.itemCount, greaterThanOrEqualTo(expectedCount))
        }

        companion object {
            fun getItemCount(expectedCount: Int): RecyclerViewItemCountAssertion {
                return RecyclerViewItemCountAssertion(expectedCount)
            }
        }
    }

}