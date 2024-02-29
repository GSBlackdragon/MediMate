package com.example.mms.service

import com.example.mms.database.inApp.AppDatabase
import com.example.mms.model.MedicineStorage
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.assertj.core.api.Assertions.*

class MedicineStorageServiceTest {
    private val dbMock = mockk<AppDatabase>()

    @Test
    fun updateMedicineStorage() {
        every { dbMock.medicineStorageDao().getMedicineStorageByMedicineId(any()) } returns MedicineStorage(1L, 10, 5)
        val actual = dbMock.medicineStorageDao().getMedicineStorageByMedicineId(1)
        val expected = MedicineStorage(1L, 10, 5)
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected)
    }
}