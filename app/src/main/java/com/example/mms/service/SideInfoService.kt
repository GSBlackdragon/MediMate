package com.example.mms.service

import android.content.Context
import android.util.Log
import com.example.mms.database.inApp.SingletonDatabase
import kotlin.reflect.typeOf

class SideInfoService(context : Context) {
    private val db = SingletonDatabase.getDatabase(context)

    fun knowIfMedicineIsInAllergicListOfUser(codeCIS: Long): Pair<Boolean,String> {
        val user = db.userDao().getConnectedUser()
        val sideInfoMedicine = db.sideInfoMedicineDao().getById(codeCIS.toString())
        if (user == null || sideInfoMedicine == null) return Pair(false,"")
        val userAllergies = user.listAllergies.split(",").toList().map { it.trim();it.lowercase() }
        val allergicOfMedicine = sideInfoMedicine.allergie.split(",").toList().map { it.lowercase();it.trim() }
        val resList = userAllergies.intersect(allergicOfMedicine.toSet())
        return Pair(resList.isNotEmpty(),resList.first())
    }

}