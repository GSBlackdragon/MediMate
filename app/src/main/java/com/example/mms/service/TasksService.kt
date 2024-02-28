package com.example.mms.service

import android.content.Context
import com.example.mms.Utils.compareLocalDateTimeOnlyDays
import com.example.mms.Utils.dateToLocalDatetime
import com.example.mms.Utils.hourMinuteToString
import com.example.mms.Utils.stringHourMinuteToInt
import com.example.mms.constant.TYPE_PRIS_PONCTUELLE
import com.example.mms.database.inApp.SingletonDatabase
import com.example.mms.model.Cycle
import com.example.mms.model.CycleHourWeight
import com.example.mms.model.HourWeight
import com.example.mms.model.MedicineStorage
import com.example.mms.model.OneTake
import com.example.mms.model.ShowableHourWeight
import com.example.mms.model.ShowableTakes
import com.example.mms.model.SpecificDaysHourWeight
import com.example.mms.model.Takes
import com.example.mms.model.Task
import com.example.mms.model.medicines.MType
import java.time.LocalDateTime
import java.util.Date

/**
 * Service to manage tasks, cycle, specific days and one take
 *
 * @param context The context of the application
 * @property db The database of the application
 */
class TasksService(context: Context) {
    private var db = SingletonDatabase.getDatabase(context)




    fun updateTask(task : Task){
        db.taskDao().update(task)
    }

    /**
     * Return a boolean regarding if the active substance is
     * already present in any active treatments during the the given period of time
     *
     * @param subcode The active substance code you want to check
     * @param startDate The start of the interval to check
     * @param endDate The end of the interval to check
     * @return The Boolean
     */
    fun isSubCodeInActiveSubstanceCode(subcode : Int?,startDate : LocalDateTime, endDate : LocalDateTime) : Boolean{
        val listSubActiveCode = mutableListOf<Int?>()
        val listTasks = getCurrentUserTasks()
        for (task in listTasks){
            if (startDate.toLocalDate() <= task.endDate.toLocalDate() && endDate.toLocalDate() >= task.startDate.toLocalDate()){
                listSubActiveCode.add(db.medicineDao().getByCIS(task.medicineCIS)?.composition?.substance_code)
            }
        }
        return (subcode in listSubActiveCode)
    }

    /**
     * Get all the tasks of the current user
     *
     * @return The list of tasks
     */
    fun getCurrentUserTasks(): List<Task> {
        val currentUserId = this.db.userDao().getConnectedUser()?.email ?: return listOf()
        return this.db.taskDao().getUserTasks(currentUserId)
    }

    /**
     * store a task in the database
     * don't store the cycle, specific days and one take
     *
     * @param task The task to store
     */
    fun storeTask(task: Task) {
        val now = LocalDateTime.now()
        task.createdAt = now
        task.updatedAt = now
        this.db.taskDao().insert(task)
    }

    /**
     * store a cycle in the database
     * don't forget to define the task id
     * 
     * @param cycle The cycle to store
     */
    fun storeCycle(cycle: Cycle) {
        // Cycle
        this.db.cycleDao().insert(cycle)
        val cycleId = this.db.cycleDao().getLastInserted()?.id ?: 0

        // get the associated task
        val task = this.db.taskDao().getTask(cycle.taskId)

        // if the cycle need to be taken today
        val shouldTake = shouldTakeThisCycleAt(task, cycle, LocalDateTime.now())

        // for all medication schedules, store the associated hourWeight
        cycle.hourWeights.forEach {
            // Insert associated hourWeight
            this.db.hourWeightDao().insert(it)
            val hourWeightId = this.db.hourWeightDao().getLastInserted()?.id ?: 0

            // CycleHourWeight
            val cycleHourWeight = CycleHourWeight(
                cycleId,
                hourWeightId
            )

            // If the cycle need to be taken today, insert a today takes
            if (shouldTake) {
                this.db.takesDao().insert(
                    Takes(
                        hourWeightId,
                        LocalDateTime.now().toLocalDate().atStartOfDay(),
                        LocalDateTime.now(),
                        false
                    )
                )
            }

            // Insert element to join cycle and hourWeight
            this.db.cycleDao().insert(cycleHourWeight)
        }
    }

    /**
     * store a specific days in the database
     *
     * @param specificDays The specific days to store
     */
    fun storeSpecificDays(specificDays: SpecificDaysHourWeight) {
        // Insert associated hourWeight
        this.db.hourWeightDao().insert(specificDays.hourWeight!!)
        val hourWeightId = this.db.hourWeightDao().getLastInserted()?.id ?: 0

        // insert the specific days
        specificDays.hourWeightId = hourWeightId
        this.db.specificDaysDao().insert(specificDays)
    }

    /**
     * store a one take in the database
     * one take is a task that need to be taken only one time
     *
     */
    fun storeOneTake(medicineCIS: Long, weight: Int) {
        val now = LocalDateTime.now()
        // Insert a task
        val task = Task(
            0,
            TYPE_PRIS_PONCTUELLE,
            now,
            now,
            now,
            now,
            false,
            medicineCIS,
            this.db.userDao().getConnectedUser()?.email!!
        )
        this.storeTask(task)
        val taskId = this.db.taskDao().getLastInserted()?.id ?: 0

        // Insert a HourWeight
        val hourWeight = HourWeight(
            0,
            "${now.hour}:${now.minute}",
            weight
        )
        this.db.hourWeightDao().insert(hourWeight)
        val hourWeightId = this.db.hourWeightDao().getLastInserted()?.id ?: 0

        // Insert a Takes
        val takes = Takes(
            hourWeightId,
            now.toLocalDate().atStartOfDay(),
            now,
            true
        )
        this.db.takesDao().insert(takes)

        // Insert a oneTake
        val oneTake = OneTake(
            taskId,
            hourWeightId
        )
        this.db.oneTakeDao().insert(oneTake)
    }

    /**
     * Get all tasks of a user that need to be taken at a specific date
     *
     * @param userId The user id
     * @param date The date
     *
     * @return The list of tasks
     */
    fun getTasksAt(userId: String, date: Date): MutableList<Task> {
        // Get all tasks of the user
        val allTasks = this.db.taskDao().getUserTasks(userId)

        val tasks = mutableListOf<Task>()

        allTasks.forEach {
            // Get the task at the given date
            // the task is empty if it doesn't need to be taken at the given date
            val task = this.getByIdAt(it.id, dateToLocalDatetime(date))

            // if the task is not empty, add it to the list
            if (!task.isEmpty()) {
                tasks.add(task)
            }
        }

        return tasks
    }

    /**
     * Get all tasks of the current user with all the informations (cycle, specific days, one take)
     */
    fun getTasksFilled(oneTakeTasks: Boolean = false) : MutableList<Task> {
        // Get all tasks of the user
        val allTasks = this.getCurrentUserTasks()

        val tasks = mutableListOf<Task>()

        allTasks.forEach {
            // Get the filled task
            val task = getTaskFilled(it, oneTakeTasks)
            if (task != null) {
                tasks.add(task)
            }
        }

        return tasks
    }

    /**
     * Get a task with all the informations (cycle, specific days, one take)
     *
     * @param task The task to fill
     * @param oneTakeTasks If true, fill the one take
     */
    fun getTaskFilled(
        task: Task,
        oneTakeTasks: Boolean = false
    ) : Task? {
        // Get the cycle and the specific days
        val cycle = this.db.taskDao().getTaskCycle(task.id)
        val specificDays = this.db.taskDao().getTaskSpecificDays(task.id)

        // If the task have a cycle, get all the hour weights
        if (cycle != null) {
            val listHourWeight = this.db.cycleDao().getCycleHourWeight(cycle.id)

            cycle.hourWeights = listHourWeight.map {
                this.db.hourWeightDao().getHourWeight(it.hourWeightId)
            }.toMutableList()

            task.cycle = cycle
        }

        // Get all the hour weights of the specific days
        for (specificDay in specificDays) {
            specificDay.hourWeight =
                this.db.hourWeightDao().getHourWeight(specificDay.hourWeightId)
            task.specificDays.add(specificDay)
        }

        // If we want to fill the one take, get the hour weight
        if (oneTakeTasks) {
            val oneTake = this.db.oneTakeDao().find(task.id)
            if (oneTake != null) {
                task.oneTakeHourWeight = this.db.hourWeightDao().getHourWeight(oneTake.hourWeightId)
            }
        }

        // return the task if it's not empty, null otherwise
        return if (task.isEmpty()) null else task
    }

    /**
     * Get a task at a the given date
     * If the task need to be taken at the given date,
     * fill the task with all the informations (cycle, specific days, one take)
     *
     * @param taskId The task id
     * @param date The date
     *
     * @return The task
     */
    fun getByIdAt(taskId: Long, date: LocalDateTime): Task {
        // Get the task, and his informations
        val task = this.db.taskDao().getTask(taskId)
        val cycle = this.db.taskDao().getTaskCycle(taskId)
        val specificDays = this.db.taskDao().getTaskSpecificDays(taskId)
        val oneTake = this.db.oneTakeDao().find(taskId)

        if (cycle != null && this.shouldTakeThisCycleAt(task, cycle, date)) {
            // If the cycle need to be taken at the given date, fill the task with cycle informations
            val listHourWeight = this.db.cycleDao().getCycleHourWeight(cycle.id)

            // Get all the hour weights of the cycle
            cycle.hourWeights = listHourWeight.map {
                this.db.hourWeightDao().getHourWeight(it.hourWeightId)
            }.toMutableList()

            task.cycle = cycle
        }

        for (specificDay in specificDays) {
            // If the specific day need to be taken at the given date, fill the task with specific day informations
            if (this.shouldTakeThisSpecificDayAt(task, specificDay, date)) {
                specificDay.hourWeight =
                    this.db.hourWeightDao().getHourWeight(specificDay.hourWeightId)
                task.specificDays.add(specificDay)
            }
        }

        // If the one take need to be taken at the given date, fill the task with one take informations
        if (oneTake != null && task.createdAt.toLocalDate() == date.toLocalDate()) {
            task.oneTakeHourWeight = this.db.hourWeightDao().getHourWeight(oneTake.hourWeightId)
        }

        return task
    }

    /**
     * Get the number of task done today and the total of task to do today
     * from a list of ShowableHourWeight
     *
     * @param listSHW The list of ShowableHourWeight
     *
     * @return A pair of Int, the first is the number of task done today, the second is the total of task to do today
     */
    fun getNumberOfTaskDoneToday(listSHW: List<ShowableHourWeight>) : Pair<Int,Int> {
        var numberTakes = 0
        var totalTakes = 0

        for (shw in listSHW) {
            // Get the Takes of the hour weight
            var takes : Takes? = null
            val tt = Thread {
                takes = this.db.takesDao().getTakes(shw.hourWeight.id, LocalDateTime.now().toLocalDate().atStartOfDay())
            }
            tt.start()
            tt.join()

            if (takes != null) {
                // If the Takes is done, increment the number of task done today
                totalTakes++
                if (takes!!.isDone) {
                    numberTakes++
                }
            }
        }
        return Pair(numberTakes, totalTakes)
    }


    /**
     * Check if a cycle need to be taken at the given date
     *
     * @param task The task containing the cycle
     * @param cycle The cycle
     * @param date The date
     *
     * @return True if the cycle need to be taken at the given date, false otherwise
     */
    fun shouldTakeThisCycleAt(task: Task, cycle: Cycle, date: LocalDateTime): Boolean {
        // Initialize dates
        val dateStartDay = date.toLocalDate().atStartOfDay()
        var pointerDate = task.updatedAt.toLocalDate().atStartOfDay()
        var lastPointerDate = pointerDate

        // If the task is created after the given date, or if the task is finished before the given date
        if (task.createdAt.toLocalDate().atStartOfDay() > dateStartDay ||
            task.startDate.toLocalDate().atStartOfDay() > dateStartDay ||
            task.endDate.toLocalDate().atStartOfDay() < dateStartDay) {
            return false
        }

        // If the task is created at the given date
        if (compareLocalDateTimeOnlyDays(pointerDate, dateStartDay)) {
            return true
        }

        // If the task is created before the given date
        // the date is after the start date of the task
        if (pointerDate < dateStartDay) {
            // while the pointer date is before the given date
            // increment the pointer date by the hours of treatment and the hours of rest
            // if the pointer date is between the last pointer date and the given date, return true
            while (pointerDate.toLocalDate().atStartOfDay() < dateStartDay) {
                // increment the pointer date by the hours of treatment
                pointerDate = pointerDate.plusHours(cycle.hoursOfTreatment.toLong())

                // if the pointerDate is between the last pointer date and the given date, return true
                // lastPointerDate and pointerDate represent the interval of time where the task need to be taken
                if (lastPointerDate <= dateStartDay && dateStartDay < pointerDate) {
                    return true
                }

                // increment the pointer date by the hours of rest
                // the pointer date is now the start of the next interval of time
                pointerDate = pointerDate.plusHours(cycle.hoursOfRest.toLong())
                lastPointerDate = pointerDate
            }
        } else {
            // If the task is created after the given date
            // the date is before the start date of the task
            // we do the same thing as before, but we decrement the pointer date

            // while the pointer date is before the given date
            while (pointerDate.toLocalDate().atStartOfDay() < dateStartDay) {
                // decrement the pointer date by the hours of rest
                pointerDate = pointerDate.minusHours(cycle.hoursOfTreatment.toLong())

                // if the pointerDate is in the interval of time where the task need to be taken
                if (pointerDate <= dateStartDay && dateStartDay < lastPointerDate) {
                    return true
                }

                // decrement the pointer date by the hours of rest
                pointerDate = pointerDate.minusHours(cycle.hoursOfRest.toLong())
                lastPointerDate = pointerDate
            }
        }

        return compareLocalDateTimeOnlyDays(pointerDate, dateStartDay)
    }


    /**
     * Check if a specific day need to be taken at the given date
     *
     * @param task The task containing the specific day
     * @param specificDay The specific day
     * @param date The date
     *
     * @return True if the specific day need to be taken at the given date, false otherwise
     */
    fun shouldTakeThisSpecificDayAt(
        task: Task,
        specificDay: SpecificDaysHourWeight,
        date: LocalDateTime
    ): Boolean {
        // If the day of the week of the specific day is the same as the day of the given date
        return date.dayOfWeek.value == specificDay.day + 1 &&
                task.createdAt.toLocalDate().atStartOfDay() < date.toLocalDate().atStartOfDay()
    }

    /**
     * Get the next takes hour weight of a cycle
     *
     * @param cycle The cycle
     *
     * @return A pair of Int, the first is the hour, the second is the minute
     */
    fun getHourNextCycle(cycle: Cycle): Pair<Int, Int> {
        for (hW in cycle.hourWeights) {
            // Get the hour and the minute of the hour weight
            val hour = hW.hour
            val splited = hour.split(":")
            val hourInt = splited[0].toInt()
            val minuteInt = splited[1].toInt()

            if (cycle.currentHourInTreatment < hourInt || (cycle.currentHourInTreatment == hourInt && cycle.currentHourInTreatment < minuteInt)) {
                return Pair(hourInt, minuteInt)
            }
        }
        return Pair(0, 0)
    }


    /**
     * Get the next date where a task need to be taken
     *
     * @param task The task
     *
     * @return The next date
     */
    fun getNextTakeDate(task: Task): LocalDateTime {

        val date = LocalDateTime.now()

        // get the next take's hour
        val hourCycle = getHourNextCycle(task.cycle)
        var nextDate = date.withHour(hourCycle.first).withMinute(hourCycle.second)

        // find the date where the task need to be taken
        if (shouldTakeThisCycleAt(task, task.cycle, nextDate)) {
            return nextDate
        }

        while (!shouldTakeThisCycleAt(task, task.cycle, nextDate)) {
            nextDate = nextDate.plusDays(1)
        }

        return nextDate
    }

    /**
     * Get or create a takes for a hour weight at a specific date
     *
     * @param hourWeightId The hour weight id
     * @param date The date
     *
     * @return The takes
     */
    fun getOrCreateTakes(hourWeightId: Int, date: LocalDateTime): Takes {
        // Get the takes at the given date
        var takes = this.db.takesDao().getTakes(hourWeightId, date.toLocalDate().atStartOfDay())

        // If the takes doesn't exist, create it
        if (takes == null) {
            this.db.takesDao()
                .insert(Takes(hourWeightId, date.toLocalDate().atStartOfDay(), date, false))
            takes = this.db.takesDao().getTakes(hourWeightId, date.toLocalDate().atStartOfDay())
        }

        return takes!!
    }

    /**
     * Create showable hour weights from a list of tasks
     * A showable hour weight is an hour weight with the name of the medicine, the type of the medicine, the task and the medicine storage
     *
     * @param tasks The list of filled tasks
     *
     * @return The list of showable hour weights
     */
    fun createShowableHourWeightsFromTasks(tasks: List<Task>): MutableList<ShowableHourWeight> {
        val items = mutableListOf<ShowableHourWeight>()

        for (task in tasks) {
            // get medicine's informations
            var medicineName = ""
            var medicineType = MType()
            var medicineStorage: MedicineStorage? = null
            val thread = Thread {
                val medicine = this.db.medicineDao().getByCIS(task.medicineCIS)
                medicineName = medicine!!.name
                medicineType = medicine.type
                medicineStorage = this.db.medicineStorageDao().getMedicineStorageByMedicineId(medicine.code_cis)
            }
            thread.start()
            thread.join()

            // Get all the hour weights for each task
            val cycleHourWeights = task.cycle.hourWeights
            val specificDaysHourWeights = mutableListOf<HourWeight>()
            task.specificDays.forEach {
                if (it.hourWeight != null) {
                    specificDaysHourWeights.add(it.hourWeight!!)
                }
            }
            val hourWeights = (cycleHourWeights + specificDaysHourWeights).toMutableList()

            if (task.oneTakeHourWeight != null) {
                hourWeights.add(task.oneTakeHourWeight!!)
            }

            // Add the ShowableHourWeight to the list
            for (hourWeight in hourWeights) {
                items.add(
                    ShowableHourWeight(
                        medicineName,
                        medicineType,
                        task,
                        hourWeight,
                        medicineStorage
                    )
                )
            }
        }

        // Sort the list by hour
        items.sortBy { it.hourWeight.hour }

        return items
    }

    /**
     * Create a list of hour weight for an interval
     * The interval is defined by the begin hour and the end hour and the interval between each hour weight
     *
     * @param interval The interval between each hour weight
     * @param beginHour The begin hour
     * @param endHour The end hour
     * @param weight The weight of each hour weight
     *
     * @return The list of hour weight
     */
    fun generateHourWeightForInterval(interval: Int, beginHour: Pair<Int, Int>, endHour: Pair<Int, Int>, weight: Int): MutableList<HourWeight> {
        val hourWeights = mutableListOf<HourWeight>()
        val now = LocalDateTime.now()
        var date = LocalDateTime.now()

        // set date to the begin hour
        date = date
            .withHour(beginHour.first)
            .withMinute(beginHour.second)
            .withSecond(0)
            .withNano(0)

        // while the date is before the end hour, add an hour weight to the list
        // the date is incremented by the interval
        while (date.dayOfMonth == now.dayOfMonth &&
               date.monthValue == now.monthValue &&
                date.year == now.year &&
            (date.hour < endHour.first || (date.hour == endHour.first && date.minute <= endHour.second)))
        {
            // add the hour weight to the list
            hourWeights.add(HourWeight(0, hourMinuteToString(date.hour, date.minute), weight))

            // increment the date
            date = date.plusHours(interval.toLong())
        }

        return hourWeights
    }

    /**
     * Get all the hour weights from a task
     *
     * @param task The task
     *
     * @return The list of hour weights
     */
    fun getAllHourWeightFromTask(task: Task): MutableList<HourWeight> {
        val hourWeights = mutableListOf<HourWeight>()

        // add the hour weights of the cycle
        hourWeights.addAll(task.cycle.hourWeights)

        // add the hour weights of the specific days
        for (specificDay in task.specificDays) {
            if (specificDay.hourWeight != null) {
                hourWeights.add(specificDay.hourWeight!!)
            }
        }

        // add the hour weight of the one take
        if (task.oneTakeHourWeight != null) {
            hourWeights.add(task.oneTakeHourWeight!!)
        }

        return hourWeights
    }

    /**
     * Get or create takes and return the list of showable hour weights
     *
     * @param userId The user id
     *
     * @return The list of showable hour weights
     */
    fun createOrGetTodaysSwHourWeight(userId: String): MutableList<ShowableHourWeight> {
        // get today's tasks
        val tasks = this.getTasksAt(userId, Date())

        // get all the hour weights for the day
        val hourWeights = this.createShowableHourWeightsFromTasks(tasks)

        // create a Takes for each hour weight
        val now = LocalDateTime.now()
        for (hourWeight in hourWeights) {
            this.getOrCreateTakes(hourWeight.hourWeight.id, now)
        }

        return hourWeights
    }

    /**
     * Create showable hour weights from one tasks
     *
     * @param task The task
     *
     * @return The list of showable hour weights
     */
    fun createOrGetOneTodaysSwHourWeight(task: Task): MutableList<ShowableHourWeight> {
        return this.createShowableHourWeightsFromTasks(listOf(task))
    }

    /**
     * Get all the hour weights for all the users for today
     *
     * @return The list of showable hour weights
     */
    fun getTodaysHourWeightsForAllUsers(): MutableList<ShowableHourWeight> {
        val allShowableHourWeights = mutableListOf<ShowableHourWeight>()
        val thread = Thread {
            val users = this.db.userDao().getAllUsers()

            for (user in users) {
                // get all the hour weights for the user
                val showableHourWeights = this.createOrGetTodaysSwHourWeight(user.email)
                allShowableHourWeights.addAll(showableHourWeights)
            }
        }
        thread.start()
        thread.join()

        return allShowableHourWeights
    }

    /**
     * remove the hour weights that are already passed for today
     *
     * @param hourWeights The list of hour weights
     *
     * @return The list of hour weights without the already passed hour weights
     */
    fun removeAlreadyPassedHourWeights(hourWeights: List<ShowableHourWeight>): MutableList<ShowableHourWeight> {
        val now = LocalDateTime.now()
        val newHourWeights = mutableListOf<ShowableHourWeight>()

        for (hourWeight in hourWeights) {
            val hourMinute = stringHourMinuteToInt(hourWeight.hourWeight.hour)

            if (now.hour < hourMinute.first || (now.hour == hourMinute.first && now.minute < hourMinute.second)) {
                newHourWeights.add(hourWeight)
            }
        }

        return newHourWeights
    }

    /**
     * Get the user's stats
     */
    fun getUserStats(): Pair<Int,Int> {
        val tasks = this.getTasksFilled()
        var totalTakes = 0
        var totalDone = 0

        for (task in tasks) {
            // for each hour weight, count the number of takes and the number of takes done
            for (hourWeight in task.cycle.hourWeights) {
                val takes = this.db.takesDao().getAllFromHourWeightId(hourWeight.id)
                if (takes.isNotEmpty()) {
                    totalTakes += takes.size
                    takes.forEach { it.isDone.let { it1 -> if (it1) totalDone++ } }
                }
            }
            for (specificDay in task.specificDays) {
                val takes = this.db.takesDao().getAllFromHourWeightId(specificDay.hourWeightId)
                if (takes.isNotEmpty()) {
                    totalTakes += takes.size
                    takes.forEach { it.isDone.let { it1 -> if (it1) totalDone++ } }
                }
            }

        }

        // return the number of takes done and the percentage of takes done
        return if (totalTakes == 0) Pair(0,0) else Pair(totalDone,(totalDone * 100) / totalTakes)
    }

    /**
     * Get all taken takes of the current user
     *
     * @return The list of showable takes
     */
    fun getCurrentUserAllTakenShowableTakes(): MutableList<ShowableTakes> {
        val shTakes = mutableListOf<ShowableTakes>()
        val thread = Thread {
            // get all the tasks of the user
            val tasks = this.getTasksFilled(true)
            // get all the hour weights from the tasks
            val showableHourWeight = this.createShowableHourWeightsFromTasks(tasks)

            for (hourWeight in showableHourWeight) {
                // get all the takes for each hour weight
                val takes = this.db.takesDao().getAllFromHourWeightId(hourWeight.hourWeight.id)

                for (take in takes) {
                    // if the take is done, add it to the list
                    if (take.isDone) {
                        shTakes.add(
                            ShowableTakes(
                                hourWeight.medicineName,
                                hourWeight.medicineType.weight ?: "",
                                take.takeAt,
                                hourWeight.hourWeight.weight
                            )
                        )
                    }
                }
            }
        }
        thread.start()
        thread.join()

        return shTakes
    }
}
