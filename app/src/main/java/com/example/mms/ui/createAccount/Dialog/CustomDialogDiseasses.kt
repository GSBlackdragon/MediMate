package com.example.mms.ui.createAccount.Dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mms.R
import com.example.mms.adapter.DialogAdapter

class CustomDialogDiseasses(
    context: Context,
    private val data: List<String>,
    private var selectedItemsList: List<String>,
    private val onItemSelected: (List<String>) -> Unit
) : Dialog(context) {

    private lateinit var adapter: DialogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_dialog_diseases)

        val recyclerView = findViewById<RecyclerView>(R.id.rv_disease)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = DialogAdapter(data) {}
        recyclerView.adapter = adapter

        adapter.updateSelection(selectedItemsList)

        val btnValidate = findViewById<Button>(R.id.btn_disease_validate)
        btnValidate.setOnClickListener {
            onItemSelected(adapter.getSelectedItems())
            dismiss()
        }

        val btnCancel = findViewById<Button>(R.id.btn_disease_cancel)
        btnCancel.setOnClickListener {
            dismiss()
        }

    }

    fun updateSelectedItems(newSelectedItems: List<String>) {
        selectedItemsList = newSelectedItems
        adapter.updateSelection(selectedItemsList)
    }


}