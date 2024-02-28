package com.example.mms.service

import android.content.Context
import com.example.mms.database.inApp.SingletonDatabase

class SideInfoService(context : Context) {
    private val db = SingletonDatabase.getDatabase(context)

    /**
     * Return a pair of a boolean and a string regarding if the current user is allergic to the medicine and if so,
     * return the active substance to which the current user is allergic to
     *
     * @param codeCIS The CIS code of the medicine to check
     * @return A pair of a Boolean (True if the user is allergic to the drug, False else) and a String (Empty string if Boolean false, the active substance involved in the allergy else)
     */
    fun knowIfMedicineIsInAllergicListOfUser(codeCIS: Long): Pair<Boolean,String> {
        val user = db.userDao().getConnectedUser()
        val sideInfoMedicine = db.sideInfoMedicineDao().getById(codeCIS.toString())
        if (user == null || sideInfoMedicine == null || sideInfoMedicine.allergie == "") return Pair(false,"")
        val userAllergies = user.listAllergies.split(",").toList().map { it.trim();it.lowercase() }
        val allergicOfMedicine = sideInfoMedicine.allergie.split(",").toList().map { it.lowercase();it.trim() }
        val resList = userAllergies.intersect(allergicOfMedicine.toSet())
        if (resList.isEmpty()) return Pair(false,"")
        return Pair(true,resList.first())
    }

}