package com.example.mms.ui.add

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mms.R
import com.example.mms.adapter.Interface.OnItemClickListener
import com.example.mms.adapter.MediProposalsAdapter
import com.example.mms.database.inApp.SingletonDatabase
import com.example.mms.databinding.ActivityAddTaskBinding
import java.io.File


class AddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTaskBinding

    private var mediProposals = mutableListOf<String>()
    private lateinit var searchBar: EditText
    private lateinit var mediProposalsAdapter: MediProposalsAdapter
    private lateinit var mediProposalsRecyclerView: RecyclerView

    private lateinit var imageUri: Uri
    private lateinit var pickImage: ActivityResultLauncher<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.buttonArrowBack.setOnClickListener {
            finish()
        }

        val takePicture: ActivityResultLauncher<Uri?> = registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                startActivity(Intent(this@AddActivity, ScanLoading::class.java)
                    .putExtra("capturedImageUri", imageUri)
                )
            }
        }

        binding.buttonScanOrdonnance.setOnClickListener {
            val permission = Manifest.permission.CAMERA
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(permission), 123)
            } else {
                val tempFile = File.createTempFile("temp_image", ".jpg", cacheDir)
                imageUri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.provider",
                    tempFile
                )
                takePicture.launch(imageUri)
            }
        }

        pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                // Faites quelque chose avec l'URI récupéré, par exemple, passez à l'activité suivante
                startActivity(Intent(this@AddActivity, ScanLoading::class.java)
                    .putExtra("capturedImageUri", uri)
                )
            }
        }

        binding.buttonGetPhoto.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permission = Manifest.permission.READ_MEDIA_IMAGES
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(permission), 456)
                } else {
                    pickImage.launch("image/*")
                }
            } else {
                val permission = Manifest.permission.READ_EXTERNAL_STORAGE
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(permission), 789)
                } else {
                    pickImage.launch("image/*")
                }
            }
        }

        binding.buttonGetPhoto.setOnLongClickListener {
            startActivity(Intent(this@AddActivity, ScanLoading::class.java)
                .putExtra("capturedImageUri", Uri.parse("android.resource://com.example.mms/drawable/ordonnance_3")))
            true
        }

        binding.addActivity.setOnClickListener {
            runOnUiThread {
                this.hideRyclerclerView()
            }
        }

        this.mediProposalsRecyclerView = findViewById(R.id.proposalList)

        this.mediProposalsAdapter = MediProposalsAdapter(this, this.mediProposals)
        this.mediProposalsAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                val medicineName = this@AddActivity.mediProposals[position]

                startActivity(
                    Intent(this@AddActivity, AddMedicamentActivity::class.java)
                        .putExtra("medicineName", medicineName)
                )
            }
        })


        this.mediProposalsRecyclerView.adapter = this.mediProposalsAdapter
        this.mediProposalsRecyclerView.layoutManager = LinearLayoutManager(this)

        this.searchBar = findViewById(R.id.medi_searchbar)
        this.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {/*Empty cause unused*/}
            override fun afterTextChanged(s: Editable?) {/*Empty cause unused*/}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSearchBar()
            }
        })
    }





    fun updateSearchBar() {
        Thread {
            val text = this.searchBar.text.toString()

            // get medicines names
            val db = SingletonDatabase.getDatabase(this)
            val medicines = db.medicineDao().getMedicinesByName(text)

            // check if medicines names changed
            if (text.isEmpty()) {
                runOnUiThread {
                    this.hideRyclerclerView()
                }
                return@Thread
            }

            // update adapter
            runOnUiThread {
                val medicinesNames = medicines.map { it.name.trim() }.toSet().toList()
                this.showRecyclerView()
                this.mediProposals.clear()
                this.mediProposals.addAll(medicinesNames)
                this.mediProposalsAdapter.notifyDataSetChanged()
            }
        }.start()
    }

    fun showRecyclerView() {
        this.mediProposalsRecyclerView.visibility = RecyclerView.VISIBLE

        val params: ViewGroup.LayoutParams = this.mediProposalsRecyclerView.layoutParams
        params.height = 700
        this.mediProposalsRecyclerView.layoutParams = params
    }

    fun hideRyclerclerView() {
        this.mediProposalsRecyclerView.visibility = RecyclerView.INVISIBLE

        val params: ViewGroup.LayoutParams = this.mediProposalsRecyclerView.layoutParams
        params.height = 0
        this.mediProposalsRecyclerView.layoutParams = params
    }

}
