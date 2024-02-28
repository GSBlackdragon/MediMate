package com.example.mms.service

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.mms.database.mongoObjects.MongoVersion
import com.example.mms.model.Doctor
import com.example.mms.model.medicines.Medicine
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


/**
 * Service to communicate with the API
 * can get medicines and version of the database
 * Singleton pattern
 *
 * @param context The context of the application
 * @property url The url of the api
 * @property queue The queue of the requests
 * @property json The json parser
 */
class ApiService private constructor(context: Context) {
    private val MedicineUrl = "http://138.68.64.36:8080/"
    private val GouvUrl = "https://gateway.api.esante.gouv.fr/fhir/v1/Practitioner?identifier="
    private val GouvKEY = "698f7862-435e-4021-a05b-8671ca5f9590"
    private val InstaMedUrl = "https://data.instamed.fr/api"
    private val queue = Volley.newRequestQueue(context)

    private val json = Json { ignoreUnknownKeys = true }
    private var Doctor :Doctor? = null
    /**
     * Add a path to the api url
     */
    private fun makeUrl(path: String,whatLink:Int): String {
        return when(whatLink){
            0 -> MedicineUrl + path
            1 -> GouvUrl + path
            2 -> InstaMedUrl + path
            else -> ""
        }
    }

    /**
     * Get the version of the database and medicines to update if the local version is not up to date
     *
     *
     * @param localVersion The version of the local database
     * @param callback The callback to call when the version and medicines to update are received
     * @param callbackError The callback to call when an error occurred
     */
    fun getMedicinesCodesToUpdate(localVersion: Int, callback: (version: MongoVersion) -> Unit, callbackError: () -> Unit) {
        // build the request
        val stringRequest = StringRequest(
            Request.Method.GET, this.makeUrl("version/$localVersion",0),
            { response ->
                try {
                    // try to parse the response into a version
                    val version = this.json.decodeFromString<MongoVersion>(response)
                    callback(version)
                } catch (e: Exception) {
                    // if an error occurred, log and call the error callback
                    Log.d("parse_json", "error: $e")
                    callbackError()
                }
            },
            {
                // if the request failed, call the error callback
                callbackError()
            })

        queue.add(stringRequest)
    }

    /**
     * Get a medicine from the database
     *
     * @param codeCis The code cis of the medicine to get
     * @param callback The callback to call when the medicine is received
     * @param errorCallback The callback to call when an error occurred
     */
    fun getMedicine(codeCis: Int, callback: (Medicine) -> Unit, errorCallback: () -> Unit) {
        // build the request
        val stringRequest = StringRequest(
            Request.Method.GET, this.makeUrl("medicine/$codeCis",0),
            { response ->
                try {
                    // try to parse the response into a medicine
                    val medicine = this.json.decodeFromString<Medicine>(response)
                    callback(medicine)
                } catch (e: Exception) {
                    Log.d("json", "error: $e")
                }
            },
            {
                // if the request failed, call the error callback
                errorCallback()
            })

        queue.add(stringRequest)
    }

    @Serializable
    private data class Bundle(
        val entry: List<Entry>
    )

    @Serializable
    private  data class Entry(
        val resource: Practitioner
    )

    @Serializable
    private data class Practitioner(
        val extension: List<Extension>
    )

    @Serializable
    private data class Extension(
        val extension: List<ExtensionDetail>? = null
    )

    @Serializable
    private data class ExtensionDetail(
        val url: String,
        val valueString: String? = null // Seul ce champ est nécessaire pour l'email
    )
    private fun extractEmail(jsonString: String): String? {


        try{
            val json = Json { ignoreUnknownKeys = true }
            val bundle = json.decodeFromString<Bundle>(jsonString)
            bundle.entry.forEach { entry ->
                entry.resource.extension.forEach { extension ->
                    extension.extension?.forEach { detail ->
                        if (detail.url == "value") {
                            return detail.valueString
                        }
                    }
                }
            }
        }catch(e:kotlinx.serialization.MissingFieldException){
            return null
        }

        return null // Retourne null si aucun email n'est trouvé
    }
    private fun getDoctorbyIDGOUV(identifier: String, callback: (String) -> Unit, errorCallback: () -> Unit) {
        val url = makeUrl(identifier, 1)
        val stringRequest = object : StringRequest(Method.GET, url,
            { response ->
                try {
                    val email = extractEmail(response)
                    if (email != null) {
                        callback(email)
                    } else {
                        callback("")
                    }
                } catch (e: Exception) {
                    Log.d("json", "error: $e")
                    errorCallback()
                }
            },
            {
                Log.d("request", "Échec de la requête")
                errorCallback()
            }) {
            override fun getHeaders(): MutableMap<String, String> = hashMapOf("ESANTE-API-KEY" to GouvKEY)
        }
        queue.add(stringRequest)
    }


    private fun getDoctorbyIDInstamed(identifier: String, callback: (Doctor) -> Unit, errorCallback: () -> Unit) {
        val url = makeUrl("/rpps/$identifier", 2)
        val stringRequest = object : StringRequest(Method.GET, url,
            { response ->
                try {
                    val doctorResponse = json.decodeFromString<TemporaryDoctor>(response)
                    // Assurez-vous que temporaryDoctor a une fonction toDoctor() définie similairement à l'exemple précédent
                    callback(doctorResponse.toDoctor())
                } catch (e: Exception) {
                    Log.d("json", "error: $e")
                    errorCallback()
                }
            },
            {
                Log.d("request", "Échec de la requête")
                errorCallback()
            }) {
            override fun getHeaders(): MutableMap<String, String> = hashMapOf("accept" to "application/json")
        }
        queue.add(stringRequest)
    }


    @Serializable
    data class DoctorsResponse(
        val doctors: List<TemporaryDoctor>
    )
    // Supposons que ces propriétés soient les seules nécessaires pour créer un objet Doctor
    @Serializable
    data class TemporaryDoctor(
        var idRpps: Long,
        var lastName: String?,
        var firstName: String?,
        var title: String?,
        var specialty: String?,
        var email: String?,
        var city: String?,
        var zipcode: String?,
        var address: String?,
        var phoneNumber: String?
    ) {
        fun toDoctor(): Doctor = Doctor(idRpps, lastName ?: "", firstName ?: "", title ?: "", specialty ?: "", email ?: "", city ?: "", zipcode ?: "", address ?: "", phoneNumber ?: "")
    }


    private fun getDoctorByName(lastName: String, firstName: String, callback: (List<Doctor>) -> Unit, errorCallback: (String) -> Unit) {
        var url = makeUrl("/rpps?page=1&_per_page=30&lastName=${lastName}&firstName=${firstName}",2)
        val stringRequest = object : StringRequest(Method.GET, url,
            { response ->
                try {
                    val doctorResponse = json.decodeFromString<List<TemporaryDoctor>>(response)
                    // Assurez-vous que temporaryDoctor a une fonction toDoctor() définie similairement à l'exemple précédent
                    callback(doctorResponse.map { it.toDoctor() })


                } catch (e: Exception) {
                    Log.d("json", "error: $e")
                    errorCallback("Erreur de traitement: ${e.message}")
                }
            },
            { error ->
                Log.d("byname", "error: $error.message")
                errorCallback("Échec de la requête: ${error.message ?: "Erreur inconnue"}")
            }) {
            override fun getHeaders(): MutableMap<String, String> = hashMapOf("accept" to "application/json")

        }
        queue.add(stringRequest)
    }



    interface DoctorResultCallback {
        fun onSuccess(doctors: List<Doctor>?)
        fun onError(error: String)
    }

    fun getDoctor(name: Pair<String, String>?, identifier: String?, resultCallback: DoctorResultCallback) {
        this.Doctor = null
        if (identifier != null) {
            getDoctorbyIDInstamed(identifier,
                callback = { doctor ->
                    this.Doctor = doctor
                    getDoctorbyIDGOUV(identifier,
                        callback = { email ->
                            this.Doctor?.email = email
                        },
                        errorCallback = {
                            Log.d("error", "Une erreur est survenue lors de la récupération de l'email du docteur")
                            // Signaler une erreur
                            resultCallback.onError("Une erreur est survenue lors de la récupération de l'email du docteur")
                        }
                    )
                    resultCallback.onSuccess(listOf(doctor))
                },
                errorCallback = {
                    Log.d("error", "Une erreur est survenue lors de la récupération des informations du docteur")
                    // Signaler une erreur
                    resultCallback.onError("Une erreur est survenue lors de la récupération des informations du docteur")
                }
            )
        } else if (name != null) {
            getDoctorByName(name.second, name.first,
                callback = { doctors ->
                    val doctorsWithEmail = mutableListOf<Doctor>()
                    val emailsReceived = MutableList(doctors.size) { false } // Suivi des emails récupérés
                    doctors.forEachIndexed { index, doctor ->
                        if (doctor.email.isNotEmpty()) {
                            doctorsWithEmail.add(doctor)
                            emailsReceived[index] = true
                            if (emailsReceived.all { it }) {
                                resultCallback.onSuccess(doctorsWithEmail)
                            }
                        } else {
                            getDoctorbyIDGOUV(doctor.rpps.toString(),
                                callback = { email ->
                                    doctor.email = email
                                    if (email.isNotEmpty()) {
                                        doctorsWithEmail.add(doctor)
                                    }
                                    emailsReceived[index] = true
                                    if (emailsReceived.all { it }) {
                                        resultCallback.onSuccess(doctorsWithEmail)
                                    }
                                },
                                errorCallback = {
                                    emailsReceived[index] = true
                                    if (emailsReceived.all { it }) {
                                        resultCallback.onSuccess(doctorsWithEmail)
                                    }
                                }
                            )
                        }
                    }
                },
                errorCallback = {
                    Log.d("error", "Une erreur est survenue lors de la récupération des informations du docteur")
                    // Signaler une erreur
                    resultCallback.onError("Une erreur est survenue lors de la récupération des informations du docteur")
                }
            )
        } else {
            // Ni identifiant ni nom fourni
            resultCallback.onSuccess(null)
        }
    }


    /* Exemple d'utilisation de la fonction getDoctor
    * Thread{
            var bd = db.doctorDao()

            ApiService.getInstance(this).getDoctor(null,"10002527652", object : ApiService.DoctorResultCallback {
                override fun onSuccess(doctor: Doctor?) {
                    if (doctor != null) {
                        Thread{ bd.insert(doctor) }.start()
                    } else {
                        Log.d("SUCCESS", "Aucun docteur trouvé")
                    }
                }

                override fun onError(error: String) {
                    Log.d("ERROR", error)
                }
            })


        }.start()
    *
    *
    *
    * */



    /**
     * Singleton pattern
     */
    companion object {
        @Volatile
        private var INSTANCE: ApiService? = null

        /**
         * get the instance of the service
         */
        fun getInstance(context: Context): ApiService =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ApiService(context).also {
                    INSTANCE = it
                }
            }
    }
}
