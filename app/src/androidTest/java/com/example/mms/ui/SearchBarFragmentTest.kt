package com.example.mms.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.mms.R
import com.example.mms.ui.add.AddActivity
import org.junit.jupiter.api.Test


class SearchBarFragmentTest {


    @Test
    fun checkTextViewContent() {
        val medicineName = "doliprane"
        ActivityScenario.launch(AddActivity::class.java)

        // the search bar is empty
        onView(withId(R.id.medi_searchbar)).check(matches(withText("")))
        // the proposal list is hidden
        onView(withId(R.id.proposalList)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)))

        // search for "doliprane"
        onView(withId(R.id.medi_searchbar)).perform(typeText(medicineName))

        // the result list is visible
        onView(withId(R.id.proposalList)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

}
