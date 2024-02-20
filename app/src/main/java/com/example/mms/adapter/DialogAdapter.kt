package com.example.mms.adapter

import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.util.set
import androidx.recyclerview.widget.RecyclerView
import com.example.mms.R


// NOT USED BECAUSE THE DISEASES ARE NOT IMPLEMENTED

/**
 * Adapter for the recycler view of the diseases
 *
 * @param dataList the list of diseases
 * @param onItemsSelected the function to call when an item is selected
 */
class DialogAdapter(
    private val dataList: List<String>,
    private val onItemsSelected: (List<String>) -> Unit
) : RecyclerView.Adapter<DialogAdapter.ViewHolder>() {

    private val selectedItems = SparseBooleanArray()

    init {
        for (i in dataList.indices) {
            selectedItems.put(i, false)
        }
    }

    /**
     * Function to get the current selected items of the dialog
     */
    fun getSelectedItems(): List<String> {
        val resultList = mutableListOf<String>()
        for (i in 0 until selectedItems.size()) {
            if (selectedItems.valueAt(i)) {
                resultList.add(dataList[selectedItems.keyAt(i)])
            }
        }
        return resultList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_diseases, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = dataList[position]
        holder.bind(currentItem, position)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBox: CheckBox = itemView.findViewById(R.id.disease_checkbox)
        private val itemName: TextView = itemView.findViewById(R.id.disease_name)

        fun bind(item: String, position: Int) {
            itemName.text = item
            checkBox.isChecked = selectedItems[position]

            checkBox.setOnCheckedChangeListener(null)
            checkBox.setOnClickListener {
                selectedItems.put(position, checkBox.isChecked)
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount() = dataList.size

    /**
     * Function updating selected values of the current dialog
     * @param selectedItemsList the list of items that must be updated as selected
     */
    fun updateSelection(selectedItemsList: List<String>) {
        selectedItemsList.forEach { selectedItem ->
            val index = dataList.indexOf(selectedItem)
            if (index != -1) {
                selectedItems[index] = true
            }
        }
    }
}