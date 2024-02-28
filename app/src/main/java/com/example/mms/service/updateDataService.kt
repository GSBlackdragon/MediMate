package com.example.mms.service

import android.content.Context
import com.example.mms.database.inApp.SingletonDatabase
import com.example.mms.database.mongoObjects.MongoVersion
import com.example.mms.model.Version

/**
 * Class to update the local database with the mongo database
 *
 * @param context the context of the application
 * @property apiService the api service to get data from the mongo database
 * @property database the local database
 * @property mongoVersion the version of the mongo database, default null
 * @property nbMedicinesToDownload the number of medicines to download
 */
class UpdateDataService(context: Context) {
    private val apiService = ApiService.getInstance(context)
    private val database = SingletonDatabase.getDatabase(context)

    private var mongoVersion: MongoVersion? = null

    var nbMedicinesToDownload = 0

    /**
     * Compare the local version with the mongo version
     *
     * @param callback The callback to call with if a update is needed
     * @param callbackError The callback to call when an error occurred
     */
    fun needToUpdate(callback: (needToUpdate: Boolean) -> Unit, callbackError: () -> Unit) {
        // get the local version from the database
        var localVersion: Version? = null
        val thread = Thread {
            localVersion = this.database.versionDao().getFirst() ?: Version(0, 0)
        }
        thread.start() // start the thread
        thread.join() // wait for the thread to finish

        // call the api to get the mongo version
        this.apiService.getMedicinesCodesToUpdate(localVersion!!.versionNumber, { mongoVersion ->
            this.mongoVersion = mongoVersion

            if (this.mongoVersion == null) {
                callback(false)
            }

            callback(this.mongoVersion!!.updatedDocumentsCis.isNotEmpty())
        }, callbackError)
    }

    /**
     * Update the local version with the mongo version
     */
    fun updateLocalVersion() {
        if (this.mongoVersion == null) {
            return
        }

        // insert the mongo version into the local database
        val thread = Thread {
            this.database.versionDao().insert(this.mongoVersion!!.toVersion())
        }
        thread.start()
        thread.join()
    }

    /**
     * Get the number of items to download
     */
    fun numberOfItemsToDownload(): Int {
        return this.mongoVersion?.updatedDocumentsCis?.size ?: 0
    }

    /**
     * Check if the update is not finish
     */
    fun isNotFinish(): Boolean {
        return !this.isFinish()
    }

    /**
     * Check if the update is finish
     */
    private fun isFinish(): Boolean {
        return this.nbMedicinesToDownload >= this.mongoVersion!!.updatedDocumentsCis.size
    }

    /**
     * Download the next medicine
     *
     * @param callback The callback to call when the medicine is downloaded
     * @param errorCallback The callback to call when an error occurred
     */
    fun nextDownload(callback: () -> Unit, errorCallback: () -> Unit) {
        // if the update is finish, call the callback
        if (this.isFinish()) {
            callback()
            return
        }

        // get the next medicine to download
        val actualMedicineCIS = this.mongoVersion!!.updatedDocumentsCis[this.nbMedicinesToDownload]

        // call the api to get the medicine
        this.apiService.getMedicine(actualMedicineCIS,
            { medicine ->
                Thread {
                    // insert the medicine into the local database
                    this.database.medicineDao().insert(medicine)
                    this.nbMedicinesToDownload++

                    callback()
                }.start()
            },
            {
                // if an error occurred, call the error callback
                errorCallback()
            }
        )
    }
}
