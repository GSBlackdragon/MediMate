package com.example.mms.ui.main

import android.Manifest
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mms.R
import com.example.mms.Utils.extractMonthAndYearFromDate
import com.example.mms.Utils.getNewCalendarDayList
import com.example.mms.Utils.stringToDate
import com.example.mms.adapter.CalendarAdapter
import com.example.mms.adapter.TakesAdapter
import com.example.mms.database.inApp.AppDatabase
import com.example.mms.database.inApp.SingletonDatabase
import com.example.mms.databinding.FragmentAccueilBinding
import com.example.mms.model.CalendarDay
import com.example.mms.model.ShowableHourWeight
import com.example.mms.model.Task
import com.example.mms.service.TasksService
import com.example.mms.ui.add.AddActivity
import com.example.mms.ui.loader.LoaderActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AccueilFragment : Fragment() {

    private var _binding: FragmentAccueilBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val today = Calendar.getInstance()

    private lateinit var calendarDays: MutableList<CalendarDay>
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var takesAdapter: TakesAdapter
    private lateinit var userMedicines: MutableList<Task>
    private lateinit var items: MutableList<ShowableHourWeight>
    private lateinit var listDoublonsSubActiveCode : MutableList<Int?>
    private lateinit var listMedicineCIS : MutableList<String>
    private lateinit var db: AppDatabase
    private lateinit var tasksService: TasksService
    private val selectedDate = Date()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        db = SingletonDatabase.getDatabase(requireContext())

        this.tasksService = TasksService(requireContext())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this.requireActivity(), arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) {
                db.userDao().getConnectedUser()
            }
            if (user == null) {
                startActivity(Intent(requireContext(), LoaderActivity::class.java))
            } else {
                viewModel.setUserData(user)
            }
        }
        _binding = FragmentAccueilBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val calendarRecyclerView = binding.calendarRecyclerView
        calendarDays = getNewCalendarDayList(mutableListOf(), today.time, this.requireContext())


        binding.calendarCurrentMonthText.text =
            SimpleDateFormat("MMMM", Locale.FRENCH).format(today.time)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        binding.calendarCurrentYearText.text =
            SimpleDateFormat("yyyy", Locale.FRENCH).format(today.time)

        val screenWith = requireActivity().windowManager.currentWindowMetrics.bounds.width()
        val itemWith = screenWith / calendarDays.size

        calendarAdapter = CalendarAdapter(inflater.context, calendarDays, itemWith)
        calendarRecyclerView.adapter = calendarAdapter
        calendarRecyclerView.layoutManager =
            LinearLayoutManager(inflater.context, LinearLayoutManager.HORIZONTAL, false)

        val medicinesRV = binding.medicRecyclerView
        userMedicines = calendarDays[1].listTasks.toMutableList()

        // We get special objects from the list of takes to show them in the view
        this.items = this.tasksService.createShowableHourWeightsFromTasks(userMedicines)

        updateSmiley()

        listDoublonsSubActiveCode = mutableListOf()
        listMedicineCIS = mutableListOf()

        val t = Thread {
            val listSubActiveCode : MutableList<Int?> = mutableListOf()
            for (item in items){
                listSubActiveCode.add(db.medicineDao().getByCIS(item.task.medicineCIS)?.composition?.substance_code)
                listMedicineCIS.add(item.task.medicineCIS.toString())
            }
            listDoublonsSubActiveCode =
                listSubActiveCode
                    .groupingBy { it }
                    .eachCount()
                    .filter { it.value >= 2 }
                    .keys
                    .toMutableList()
        }
        t.start()
        t.join()


        setMonthAndYear(extractMonthAndYearFromDate(this.selectedDate.toString())!!.first, extractMonthAndYearFromDate(this.selectedDate.toString())!!.second)
        takesAdapter = TakesAdapter(root.context, items, db, this.selectedDate, root,listDoublonsSubActiveCode,listMedicineCIS) { updateSmiley() }
        medicinesRV.layoutManager = LinearLayoutManager(root.context)
        medicinesRV.adapter = takesAdapter

        calendarAdapter.setOnItemClickListener { position ->
            val clickedDay = calendarDays[position]
            val newCalendarDays = getNewCalendarDayList(
                calendarDays,
                stringToDate(clickedDay.date),
                this@AccueilFragment.requireContext()
            )

            calendarDays.clear()
            calendarDays.addAll(newCalendarDays)
            calendarAdapter.toggleDaySelection(calendarDays.indexOfFirst { it.date == clickedDay.date })
            calendarAdapter.notifyDataSetChanged()

            userMedicines.clear()
            userMedicines.addAll(clickedDay.listTasks)
            items.clear()
            items.addAll(
                this@AccueilFragment.tasksService.createShowableHourWeightsFromTasks(
                    userMedicines
                )
            )
            takesAdapter.updateListDoublonsAndCIS(updateSubActiveDoublons())

            takesAdapter.updateCurrentDate(stringToDate(clickedDay.date))

            takesAdapter.notifyDataSetChanged()


            if (clickedDay.date != today.time.toString()) binding.floatingActionButtonBackToday.show()
            else binding.floatingActionButtonBackToday.hide()

            updateSmiley()
        }

        calendarAdapter.setCalendarAdapterInterface { month, year -> setMonthAndYear(month, year) }


        binding.buttonNextMonth.setOnClickListener {
            updateMonth(1)
        }

        binding.buttonPreviousMonth.setOnClickListener {
            updateMonth(-1)
        }

        binding.floatingActionButtonBackToday.hide()

        binding.floatingActionButtonBackToday.setOnClickListener {
            today.time = Date()
            val newCalendarDays =
                getNewCalendarDayList(calendarDays, today.time, this.requireContext())

            calendarDays.clear()
            calendarDays.addAll(newCalendarDays)
            val clickedDay = calendarDays.find { it.date == today.time.toString() }!!
            calendarAdapter.toggleDaySelection(calendarDays.indexOfFirst { it.date == today.time.toString() })
            setMonthAndYear(
                extractMonthAndYearFromDate(today.time.toString())?.first.orEmpty(),
                extractMonthAndYearFromDate(today.time.toString())?.second.orEmpty()
            )
            calendarAdapter.notifyDataSetChanged()

            userMedicines.clear()
            userMedicines.addAll(clickedDay.listTasks)
            items.clear()
            items.addAll(this.tasksService.createShowableHourWeightsFromTasks(userMedicines))
            takesAdapter.updateListDoublonsAndCIS(updateSubActiveDoublons())
            takesAdapter.updateCurrentDate(stringToDate(clickedDay.date))
            takesAdapter.notifyDataSetChanged()
            binding.floatingActionButtonBackToday.hide()

            updateSmiley()
        }

        binding.floatingActionButtonAddMedic.setOnClickListener {
            startActivity(Intent(root.context, AddActivity::class.java))
        }

        return root
    }

    private fun updateSmiley() {
        val numberTakesTook = this.tasksService.getNumberOfTaskDoneToday(items)
        val now = Date()

        if (!areDatesOnSameDay(now, selectedDate)) {
            binding.imageView.setImageResource(R.drawable.tres_heureux)
            binding.textHome.text = getString(R.string.vous_aurez_medicament_prendre, numberTakesTook.second.toString())
            return
        }

        if (numberTakesTook.second == 0 || items.isEmpty()) {
            binding.imageView.setImageResource(R.drawable.tres_heureux)
            binding.textHome.text = getString(R.string.rien_prendre_aujourd_hui)
        } else {
            val percent = numberTakesTook.first * 100 / numberTakesTook.second
            when {
                percent < 25 -> binding.imageView.setImageResource(R.drawable.en_colere)
                percent < 50 -> binding.imageView.setImageResource(R.drawable.tres_triste)
                percent < 75 -> binding.imageView.setImageResource(R.drawable.neutre)
                else -> binding.imageView.setImageResource(R.drawable.tres_heureux)
            }
            binding.textHome.text =
                getString(R.string.vous_avez_pris_medicaments_aujourd_hui, numberTakesTook.first.toString())
        }
    }

    private fun updateMonth(numberToAdd: Int) {
        val newCalendar = Calendar.getInstance()
        newCalendar.time = stringToDate(getDaySelected().date)
        newCalendar.add(Calendar.MONTH, numberToAdd)

        val newCalendarDays =
            getNewCalendarDayList(calendarDays, newCalendar.time, this.requireContext())

        calendarDays.clear()
        calendarDays.addAll(newCalendarDays)
        val clickedDay = calendarDays.find { it.date == newCalendar.time.toString() }!!
        calendarAdapter.toggleDaySelection(calendarDays.indexOfFirst { it.date == newCalendar.time.toString() })
        setMonthAndYear(
            extractMonthAndYearFromDate(newCalendar.time.toString())?.first.orEmpty(),
            extractMonthAndYearFromDate(newCalendar.time.toString())?.second.orEmpty()
        )

        userMedicines.clear()
        userMedicines.addAll(clickedDay.listTasks)
        items.clear()
        items.addAll(this.tasksService.createShowableHourWeightsFromTasks(userMedicines))

        takesAdapter.updateListDoublonsAndCIS(updateSubActiveDoublons())

        takesAdapter.updateCurrentDate(stringToDate(clickedDay.date))
        takesAdapter.notifyDataSetChanged()

        calendarAdapter.notifyDataSetChanged()

        if (!areDatesOnSameDay(newCalendar.time, Date()))
            binding.floatingActionButtonBackToday.show()
        else
            binding.floatingActionButtonBackToday.hide()

        updateSmiley()
    }

    private fun setMonthAndYear(month: String, year: String) {
        binding.calendarCurrentMonthText.text = month
        binding.calendarCurrentYearText.text = year
    }


    private fun areDatesOnSameDay(date1: Date, date2: Date): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate1 = dateFormat.format(date1)
        val formattedDate2 = dateFormat.format(date2)

        return formattedDate1 == formattedDate2
    }

    private fun getDaySelected(): CalendarDay {
        return calendarDays.find { it.isSelected }!!
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateSubActiveDoublons() : Pair<MutableList<Int?>,MutableList<String>> {

        listDoublonsSubActiveCode.clear()
        listMedicineCIS.clear()


        val t = Thread {
            val listSubActiveCode : MutableList<Int?> = mutableListOf()
            for (item in items){
                listSubActiveCode.add(db.medicineDao().getByCIS(item.task.medicineCIS)?.composition?.substance_code)
                listMedicineCIS.add(item.task.medicineCIS.toString())
            }
            listDoublonsSubActiveCode =
                listSubActiveCode
                    .groupingBy { it }
                    .eachCount()
                    .filter { it.value >= 2 }
                    .keys
                    .toMutableList()

        }
        t.start()
        t.join()
        return Pair(listDoublonsSubActiveCode,listMedicineCIS)
    }
}
