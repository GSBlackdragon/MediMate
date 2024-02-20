package com.example.mms.Utils

import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.mms.R

/**
 * Function to navigate between fragments
 * @param activity
 * @param fragmentMove
 */
fun goTo(activity : FragmentActivity, fragmentMove: Int) {
    val navHostFragment =
        activity.supportFragmentManager.findFragmentById(R.id.nav_add_medicament) as NavHostFragment
    val navController = navHostFragment.navController
    navController.navigate(fragmentMove)
}