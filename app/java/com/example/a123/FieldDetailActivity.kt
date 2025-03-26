package com.example.a123

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import android.view.View
import android.util.Log
import android.os.Build
import android.provider.Settings



class FieldDetailActivity : AppCompatActivity() {

    private lateinit var xmlHelper: FieldXmlHelper
    private lateinit var fieldData: FieldData
    private lateinit var fieldId: String
    private val tasks = mutableListOf<TaskData>() // Список задач с датами и временем

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_field_detail)

        xmlHelper = FieldXmlHelper(this)

        // Получаем ID поля из Intent
        fieldId = intent.getStringExtra("fieldId") ?: return

        // Загружаем поле по ID
        fieldData = xmlHelper.loadFields().find { it.id == fieldId } ?: return

        val taskListView: ListView = findViewById(R.id.listView_tasks)


        // Инициализация адаптера задач
        val taskAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, tasks.map { it.toString() })
        taskListView.adapter = taskAdapter


        tasks.addAll(fieldData.tasks)
        taskAdapter.addAll(tasks.map { it.toString() })
        taskAdapter.notifyDataSetChanged()



        Log.d("FieldDetailActivity", "Загруженные данные поля: $fieldData")
        Log.d("FieldDetailActivity", "Загруженные задачи: ${fieldData.tasks}")

        // Инициализируем UI-элементы
        val fieldNameTextView: TextView = findViewById(R.id.textView_field_name)
        val fieldAreaTextView: TextView = findViewById(R.id.textView_field_area)
        val addTaskButton: Button = findViewById(R.id.button_add_task)

        val cropSpinner: Spinner = findViewById(R.id.spinner_crop)
        val soilTypeSpinner: Spinner = findViewById(R.id.spinner_soil_type)

        setupCropSpinner(cropSpinner)
        setupSoilTypeSpinner(soilTypeSpinner)

        cropSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                fieldData.crop = cropSpinner.selectedItem.toString()
                xmlHelper.updateField(fieldData) // Сохраняем изменения в XML
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        soilTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                fieldData.soilType = soilTypeSpinner.selectedItem.toString()
                xmlHelper.updateField(fieldData) // Сохраняем изменения в XML
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        cropSpinner.setSelection(getSpinnerIndex(cropSpinner, fieldData.crop))
        soilTypeSpinner.setSelection(getSpinnerIndex(soilTypeSpinner, fieldData.soilType))

        // Функция для получения индекса выбранного элемента






        // Установка информации о поле
        fieldNameTextView.text = fieldData.name
        fieldAreaTextView.text = String.format("%.2f Га", fieldData.area)


        // Обработка добавления новой задачи
        addTaskButton.setOnClickListener {
            showAddTaskDialog { task, calendar ->
                tasks.add(TaskData(task, calendar.timeInMillis)) // Добавляем задачу с датой и временем
                Log.d("FieldDetailActivity", "Задача добавлена: $task с датой $calendar")

                // Сохраняем задачи в данных поля
                fieldData.tasks = tasks
                xmlHelper.updateField(fieldData) // Обновляем данные в XML
                Log.d("FieldDetailActivity", "Задачи сохранены в поле: ${fieldData.tasks}")

                // Обновляем адаптер для отображения задач
                taskAdapter.add(TaskData(task, calendar.timeInMillis).toString()) // Добавляем только новую задачу
                taskAdapter.notifyDataSetChanged() // Обновляем список
                Log.d("FieldDetailActivity", "Адаптер обновлен с задачами: ${tasks.map { it.toString() }}")

                scheduleNotification(task, calendar) // Установка уведомления
            }
        }




        // Удаление задач по нажатию
        taskListView.setOnItemClickListener { _, _, position, _ ->
            val task = tasks[position]
            showDeleteTaskDialog(task.description) {
                // Удаляем задачу из списка
                tasks.remove(task)

                // Обновляем задачи в fieldData и сохраняем
                fieldData.tasks = tasks
                xmlHelper.updateField(fieldData) // Сохраняем изменения в XML

                // Обновляем адаптер
                taskAdapter.clear() // Очищаем адаптер
                taskAdapter.addAll(tasks.map { it.toString() }) // Добавляем обновленный список
                taskAdapter.notifyDataSetChanged() // Обновляем отображение
            }
        }

    }

    private fun setupCropSpinner(spinner: Spinner) {
        val cropOptions = listOf("Пшеница", "Кукуруза", "Подсолнечник", "Соя", "Картофель", "Рапс")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cropOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setupSoilTypeSpinner(spinner: Spinner) {
        val soilTypeOptions = listOf("Чернозем", "Суглинок", "Песчаная почва", "Глинистая почва")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, soilTypeOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }



    private fun getSpinnerIndex(spinner: Spinner, value: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == value) {
                return i
            }
        }
        return 0
    }


    // Диалог для добавления новой задачи с датой и временем
    private fun showAddTaskDialog(onTaskAdded: (String, Calendar) -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Добавить задачу")

        val input = EditText(this)
        input.hint = "Введите описание задачи"
        builder.setView(input)

        builder.setPositiveButton("Далее") { _, _ ->
            val task = input.text.toString()
            if (task.isNotEmpty()) {
                // Показываем DatePickerDialog для выбора даты
                showDatePicker { selectedDate ->
                    // Показываем TimePickerDialog для выбора времени
                    showTimePicker { selectedTime ->
                        selectedDate.set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY))
                        selectedDate.set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE))
                        onTaskAdded(task, selectedDate)
                    }
                }
            }
        }

        builder.setNegativeButton("Отмена") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    // Диалог для удаления задачи
    private fun showDeleteTaskDialog(task: String, onTaskDeleted: () -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Удалить задачу")
        builder.setMessage("Вы действительно хотите удалить задачу: \"$task\"?")

        builder.setPositiveButton("Да") { _, _ ->
            onTaskDeleted()
        }

        builder.setNegativeButton("Нет") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    // Диалог для выбора даты
    private fun showDatePicker(onDateSelected: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(selectedYear, selectedMonth, selectedDay)
            onDateSelected(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    // Диалог для выбора времени
    private fun showTimePicker(onTimeSelected: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val selectedTime = Calendar.getInstance()
            selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour)
            selectedTime.set(Calendar.MINUTE, selectedMinute)
            onTimeSelected(selectedTime)
        }, hour, minute, true)

        timePickerDialog.show()
    }

    // Установка уведомления с использованием AlarmManager
    private fun scheduleNotification(task: String, calendar: Calendar) {
        val intent = Intent(this, TaskNotificationReceiver::class.java)
        intent.putExtra("task", task)

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            task.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
        Log.d("FieldDetailActivity", "Будильник установлен на ${calendar.time}")

    }



}
