package com.example.mms.service

import android.content.Context
import com.example.mms.database.inApp.AppDatabase
import com.example.mms.model.SideInfoMedicine
import com.example.mms.model.User
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test

internal class SideInfoServiceTest {
    private val contextMock = mockk<Context>(relaxed = true)
    private val dbMockk = mockk<AppDatabase>()

    @Test
    fun knowIfMedicineIsInAllergicListOfUserCISNotFound() {
        val sideInfoService = SideInfoService(contextMock, dbMockk)
        every { dbMockk.userDao().getConnectedUser() } returns User(
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
            "",
            false
        )
        every { dbMockk.sideInfoMedicineDao().getById(any()) } returns null
        val result = sideInfoService.knowIfMedicineIsInAllergicListOfUser(123456789)
        assertEquals(false, result.first)
        assertEquals("", result.second)
    }

    @Test
    fun knowIfMedicineIsInAllergicListOfUserUserNotFound() {
        val sideInfoService = SideInfoService(contextMock, dbMockk)
        every { dbMockk.userDao().getConnectedUser() } returns null
        every { dbMockk.sideInfoMedicineDao().getById(any()) } returns null
        val result = sideInfoService.knowIfMedicineIsInAllergicListOfUser(123456789)
        assertEquals(false, result.first)
        assertEquals("", result.second)
    }

    @Test
    fun knowIfMedicineIsInAllergicListOfUserNoAllergies() {
        val sideInfoService = SideInfoService(contextMock, dbMockk)
        every { dbMockk.userDao().getConnectedUser() } returns User(
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
            "blé",
            false
        )
        every {
            dbMockk.sideInfoMedicineDao().getById("64728712")
        } returns SideInfoMedicine("64728712", "", "blé", "", "")
        val result = sideInfoService.knowIfMedicineIsInAllergicListOfUser(64728712)
        assertEquals(true, result.first)
        assertEquals("blé", result.second)
    }
}