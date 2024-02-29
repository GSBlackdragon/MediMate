package com.example.mms.ui.main

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mms.R
import com.example.mms.adapter.DoctorAdapter
import com.example.mms.adapter.TempDoctorAdapter
import com.example.mms.constant.LIEN_EFFETS_INDESIRABLES
import com.example.mms.database.inApp.SingletonDatabase
import com.example.mms.databinding.FragmentConseilsBinding
import com.example.mms.model.Doctor
import com.example.mms.service.ApiService
import com.google.android.gms.location.FusedLocationProviderClient
import org.osmdroid.api.IMapController
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class ConseilsFragment : Fragment() {

    private var _binding: FragmentConseilsBinding? = null
    private lateinit var controller: IMapController
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var doctorsAdded: MutableList<Doctor>
    private lateinit var adapterDoctor : DoctorAdapter

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        ViewModelProvider(requireActivity())[MainViewModel::class.java]

        _binding = FragmentConseilsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val db = SingletonDatabase.getDatabase(this.requireContext())
        val tt = Thread {
            val doctorDao = db.doctorDao()
            doctorsAdded = doctorDao.getAll()?.toMutableList() ?: mutableListOf()
        }
        tt.start()
        tt.join()

        adapterDoctor = DoctorAdapter(doctorsAdded.toMutableList(), this.requireContext(),db)
        binding.listMedecins.adapter = adapterDoctor
        binding.listMedecins.layoutManager = LinearLayoutManager(this.requireContext())



        binding.ajoutMedecin.setOnClickListener {
            dialogAddDoctor()
        }

        binding.effetIndesirable.setOnClickListener {
            // open link into browser
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(LIEN_EFFETS_INDESIRABLES)
            startActivity(i)
        }


        return root
    }
    // Geffroy Pascale
    private fun dialogAddDoctor() {
        val dialog = Dialog(this.requireContext())
        val api = ApiService.getInstance(this.requireContext())
        dialog.setContentView(R.layout.custom_dialog_add_doctor)
        val btnSearch       = dialog.findViewById<Button>(R.id.btn_search_doctor)
        val nameDoctor      = dialog.findViewById<EditText>(R.id.et_search_doctor_name)
        val firstNameDoctor = dialog.findViewById<EditText>(R.id.et_search_doctor_firstname)
        val idDoctor        = dialog.findViewById<EditText>(R.id.et_search_doctor_id)
        val btnCancel       = dialog.findViewById<Button>(R.id.btn_cancel)
        val progressBar     = dialog.findViewById<ProgressBar>(R.id.progressBar_doctor_search)
        progressBar.visibility = View.GONE
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        btnSearch.setOnClickListener {
            btnSearch.isEnabled = false
            nameDoctor.isEnabled = false
            firstNameDoctor.isEnabled = false
            idDoctor.isEnabled = false
            progressBar.visibility = View.VISIBLE
            progressBar.isIndeterminate = true
            // Call API
            if(nameDoctor.text.isEmpty() && idDoctor.text.isEmpty()) {
                Toast.makeText(this.requireContext(), getString(R.string.fill_fields), Toast.LENGTH_SHORT).show()
            } else {
                var id : String? = idDoctor.text.toString()
                if (id!!.isEmpty()) id = null
                val lastName = nameDoctor.text.toString()
                val firstName = firstNameDoctor.text.toString()
                api.getDoctor(Pair(firstName,lastName),id, object : ApiService.DoctorResultCallback{
                    override fun onSuccess(doctors: List<Doctor>?) {
                        btnSearch.isEnabled = true
                        progressBar.visibility = View.GONE
                        nameDoctor.isEnabled = true
                        firstNameDoctor.isEnabled = true
                        idDoctor.isEnabled = true
                        if (!doctors.isNullOrEmpty()) {
                            dialogChooseDoctors(doctors)
                        }else {
                            Toast.makeText(this@ConseilsFragment.requireContext(), R.string.no_doctor_found, Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onError(error: String) {
                        btnSearch.isEnabled = true
                        progressBar.visibility = View.GONE
                        nameDoctor.isEnabled = true
                        firstNameDoctor.isEnabled = true
                        idDoctor.isEnabled = true
                        Toast.makeText(this@ConseilsFragment.requireContext(), R.string.error, Toast.LENGTH_SHORT).show()
                        Log.d("ERROR", error)
                    }
                })
            }
        }
        dialog.show()
    }

    private fun dialogChooseDoctors(doctors: List<Doctor>) {
        val dialog = Dialog(this.requireContext())
        dialog.setContentView(R.layout.custom_dialog_choose_doctor)
        val btnValidate = dialog.findViewById<Button>(R.id.btn_search_doctor)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)

        val recyclerView = dialog.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_list_doctors)
        val adapter = TempDoctorAdapter(doctors.toMutableList(), this.requireContext())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this.requireContext())

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        val doctorsChecked = mutableListOf<Doctor>()
        adapter.setOnItemClickListener { position ->
            val doctor = doctors[position]
            if (doctorsChecked.contains(doctor)) {
                doctorsChecked.remove(doctor)
            } else {
                doctorsChecked.add(doctor)
            }
        }
        btnValidate.setOnClickListener {
            if (doctorsChecked.isEmpty()) {
                Toast.makeText(this.requireContext(), R.string.choose_doctor, Toast.LENGTH_SHORT).show()
            } else {
                val db = SingletonDatabase.getDatabase(this.requireContext())
                val tt = Thread {
                    val doctorDao = db.doctorDao()
                    doctorDao.insertMany(doctorsChecked)
                    doctorsAdded = db.doctorDao().getAll()?.toMutableList() ?: mutableListOf()
                }
                tt.start()
                tt.join()
                this.adapterDoctor.updateList(doctorsAdded)
                this.adapterDoctor.notifyDataSetChanged()
                Toast.makeText(this.requireContext(), R.string.doctor_added, Toast.LENGTH_SHORT).show()
                Log.d("DOCTORS", doctorsAdded.size.toString())
                dialog.dismiss()


            }
        }
        dialog.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation()
            } else {
                Toast.makeText(this.requireContext(), getString(R.string.permission_refusee), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestLocation() {
        if (ContextCompat.checkSelfPermission(this.requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                location -> location?.let {
                    val currentLocation = GeoPoint(location.latitude, location.longitude)
                    controller.setCenter(currentLocation)
                    controller.animateTo(currentLocation)

                    myLocationOverlay.enableMyLocation()
                    myLocationOverlay.enableFollowLocation()
                    myLocationOverlay.isDrawAccuracyEnabled = true
                    myLocationOverlay.runOnFirstFix {
                        Looper.myLooper()?.let { _ -> controller.animateTo(myLocationOverlay.myLocation) }
                    }
                }
            }
        } else {
            ActivityCompat.requestPermissions(this.requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// MAP PART NOT FINISHED

/**map = binding.itemMap.osmmap
map.setTileSource(TileSourceFactory.USGS_SAT)
map.setMultiTouchControls(true)
map.mapCenter
map.getLocalVisibleRect(Rect())

myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this.requireContext()), map)
controller = map.controller

fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())
requestLocation()

controller.setZoom(15.0)
map.overlays.add(myLocationOverlay)

map.addMapListener(object : MapListener {
override fun onScroll(event: ScrollEvent?): Boolean {
// event?.source?.getMapCenter()
Log.e("CC", "onCreate:la ${event?.source?.mapCenter?.latitude}")
Log.e("CC", "onCreate:lo ${event?.source?.mapCenter?.longitude}")
//  Log.e("TAG", "onScroll   x: ${event?.x}  y: ${event?.y}", )
return true
}

override fun onZoom(event: ZoomEvent?): Boolean {
//  event?.zoomLevel?.let { controller.setZoom(it) }
Log.e("CC", "onZoom zoom level: ${event?.zoomLevel}   source:  ${event?.source}")
return false;
}
})**/