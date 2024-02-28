package com.example.mms.ui.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mms.model.Cycle
import com.example.mms.model.MedicineStorage
import com.example.mms.model.SpecificDaysHourWeight
import com.example.mms.model.Task

class SharedAMViewModel : ViewModel() {
    private val _taskData = MutableLiveData(Task())
    val taskData: LiveData<Task> get() = _taskData

    private val _cycle = MutableLiveData<Cycle?>()
    val cycle: LiveData<Cycle?> get() = _cycle

    private val _specificDays = MutableLiveData<MutableList<SpecificDaysHourWeight>>()
    val specificDays: LiveData<MutableList<SpecificDaysHourWeight>> get() = _specificDays

    private val _medicineName = MutableLiveData("")
    val medicineName: LiveData<String> get() = _medicineName

    private val _oneTakeWeight = MutableLiveData(1)
    val oneTakeWeight: LiveData<Int> get() = _oneTakeWeight

    private val _previousFragmentId = MutableLiveData(0)
    val previousFragmentId: LiveData<Int> get() = _previousFragmentId

    private val _storage = MutableLiveData<MedicineStorage?>(null)
    val storage: LiveData<MedicineStorage?> get() = _storage

    private val _storageIsChecked = MutableLiveData(false)
    val storageIsChecked: LiveData<Boolean> get() = _storageIsChecked

    private val _fromOCR = MutableLiveData(false)
    val fromOCR: LiveData<Boolean> get() = _fromOCR

    fun setTask(task: Task) {
        _taskData.value = task
    }

    fun setMedicineName(string: String) {
        _medicineName.value = string
    }

    fun setCycle(cycle: Cycle?) {
        _cycle.value = cycle
    }

    fun setSpecificDays(specificDays: List<SpecificDaysHourWeight>) {
        _specificDays.value = specificDays.toMutableList()
    }

    fun setOneTakeWeight(weight: Int) {
        _oneTakeWeight.value = weight
    }

    fun setStorage(storage: MedicineStorage) {
        _storage.value = storage
    }

    fun setPreviousFragmentId(id: Int) {
        _previousFragmentId.value = id
    }

    fun setFromOCR(fromOCR: Boolean) {
        _fromOCR.value = fromOCR
    }

    fun setStorageIsChecked(isChecked: Boolean) {
        _storageIsChecked.value = isChecked
    }

    fun clearFrequencyData() {
        _cycle.value = null
        _specificDays.value = mutableListOf()
        _oneTakeWeight.value = 1
    }

}
