package com.example.mms.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mms.model.medicines.Medicine

@Dao
interface MedicineDAO {
    @Query("SELECT * FROM Medicine")
    fun getAll(): List<Medicine>

    @Query("SELECT COUNT(*) FROM Medicine")
    fun getNbElements(): Int

    @Query("SELECT * FROM Medicine WHERE code_cis = :codeCis")
    fun getByCIS(codeCis: Long): Medicine?

    @Query("SELECT name FROM Medicine WHERE code_cis = :codeCis")
    fun getNameByCIS(codeCis: Long): String? {
        return getByCIS(codeCis)?.name
    }

    @Query("SELECT * FROM Medicine WHERE name = :name AND type_generic = :type AND type_weight = :weight LIMIT 1")
    fun getByNameTypeWeight(name: String, type: String, weight: String): Medicine?

    @Query("SELECT * FROM Medicine WHERE name LIKE '%' || :name || '%' LIMIT 20")
    fun getMedicinesByName(name: String): List<Medicine>
    @Query("SELECT code_cis FROM Medicine WHERE name = :name LIMIT 1")
    fun getMedicineIdByName(name: String) : Long

    @Query("SELECT * FROM Medicine LIMIT 1 OFFSET :offset")
    fun getOneAt(offset: Int): Medicine

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(medicine: Medicine)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMany(medicines: List<Medicine>)

    @Query("DELETE FROM Medicine")
    fun deleteAll()
}
