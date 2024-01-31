package com.example.mms.database.csvDoctor

import android.content.Context
import android.util.Log
import com.example.mms.dao.DoctorDAO
import com.example.mms.dao.MedicineDAO
import com.example.mms.model.Doctor
import com.example.mms.model.medicines.Medicine
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream


class DoctorCsvDatabase(val context: Context
) {
    private val dbFileName = "databases/doctor_flat.csv"

    /**
     * Get the csv content from the file and insert it into the room database
     */
    fun transferCsvDBintoRoom(db: DoctorDAO) {/*
        var file = this.context.assets.open("databases/doctor_flat.csv").bufferedReader()

        file.use { reader ->
            val batchSize = 100
            val batch = mutableListOf<Doctor>()

            reader.forEachLine { line ->
                if (line[0] != 'T') {
                    val tokens = line.split(";")
                    batch.add(
                        Doctor(
                            tokens[0].toLong(),
                            tokens[1].toLong(),
                            tokens[2],
                            tokens[3],
                            tokens[4],
                            tokens[5],
                            tokens[6]
                        )
                    )
                    if (batch.size >= batchSize) {
                        db.insertMany(batch)
                        batch.clear()
                    }
                }
            }

            if (batch.isNotEmpty()) {
                db.insertMany(batch)
            }

        }*/


        for(i in 1..3){

            var reader = this.context.assets.open("databases/split_$i.csv").bufferedReader()
            var content = mutableListOf<Doctor>()
            reader.lineSequence()
                .drop(1)
                .filter { it.isNotBlank() }
                .map {
                    val res = it.split(';', ignoreCase = false, limit = 8)

                    content.add(Doctor(res[1].toLong(), res[2].toLong(),res[3],res[4],res[5],res[6],res[7]))
                    if (content.size >= 500) {
                        db.insertMany(content)
                        content.clear()
                    }
                }.toList()

        }
    }




}



