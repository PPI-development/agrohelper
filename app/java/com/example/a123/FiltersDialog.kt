// FiltersDialog.kt
package com.example.a123

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.*
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.util.*
import java.text.SimpleDateFormat

class FiltersDialog(
    context: Context,
    private val currentFilters: ReportFilters,
    private val onFiltersApplied: (ReportFilters) -> Unit
) : Dialog(context) {

    private lateinit var dateTextView: TextView
    private lateinit var activityTypeSpinner: Spinner
    private lateinit var workTypeSpinner: Spinner
    private lateinit var workPlaceSpinner: Spinner
    private lateinit var regionSpinner: Spinner
    private lateinit var districtSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_filters)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        // Инициализируем все элементы интерфейса
        dateTextView = findViewById(R.id.textViewDate)
        activityTypeSpinner = findViewById(R.id.spinnerActivityType)
        workTypeSpinner = findViewById(R.id.spinnerWorkType)
        workPlaceSpinner = findViewById(R.id.spinnerWorkPlace)
        regionSpinner = findViewById(R.id.spinnerRegion)
        districtSpinner = findViewById(R.id.spinnerDistrict)

        // Установка текущих значений фильтров
        dateTextView.text = currentFilters.date

        // Настройка обработчиков и спиннеров
        setupDatePicker()
        setupSpinners()
        setupButtons()
    }

    private fun setupDatePicker() {
        dateTextView.setOnClickListener {
            showDatePickerDialog()
        }

        // Устанавливаем текущую дату в TextView, если она есть
        if (currentFilters.date.isNotEmpty()) {
            dateTextView.text = currentFilters.date
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        // Если дата уже выбрана, устанавливаем ее в календаре
        if (currentFilters.date.isNotEmpty()) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = sdf.parse(currentFilters.date)
            if (date != null) {
                calendar.time = date
            }
        }
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                dateTextView.text = selectedDate
                currentFilters.date = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun setupSpinners() {
        // Инициализируем данные для спиннеров
        val activityTypes = listOf("", "Энтомолог", "Фитопатолог", "Герболог")
        val workTypes = listOf("", "Первичный мониторинг", "Проведение осмотра Вредных Организмов")
        val workPlaces = listOf("", "Поле", "Сад", "Теплица", "Лесопосадка")
        val regions = listOf("") + ReportUtils.getRegions()
        val districts = listOf<String>() // Изначально пустой список

        // Настраиваем адаптеры для спиннеров
        activityTypeSpinner.adapter = createBlackTextAdapter(activityTypes)
        workTypeSpinner.adapter = createBlackTextAdapter(workTypes)
        workPlaceSpinner.adapter = createBlackTextAdapter(workPlaces)
        regionSpinner.adapter = createBlackTextAdapter(regions)
        districtSpinner.adapter = createBlackTextAdapter(districts) // Пустой адаптер

        // Устанавливаем текущие значения
        setSpinnerSelection(activityTypeSpinner, currentFilters.activityType)
        setSpinnerSelection(workTypeSpinner, currentFilters.workType)
        setSpinnerSelection(workPlaceSpinner, currentFilters.workPlace)
        setSpinnerSelection(regionSpinner, currentFilters.region)
        // Установка района будет после выбора области

        // Обработка изменений области для обновления районов
        regionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedRegion = regionSpinner.selectedItem?.toString() ?: ""
                val districts = listOf("") + ReportUtils.getDistrictsForRegion(selectedRegion)
                districtSpinner.adapter = createBlackTextAdapter(districts)
                // Устанавливаем выбранный район только после установки адаптера
                setSpinnerSelection(districtSpinner, currentFilters.district)
            }
        }

        // Если область уже выбрана, обновляем список районов и устанавливаем выбранный район
        if (currentFilters.region.isNotEmpty()) {
            val districts = listOf("") + ReportUtils.getDistrictsForRegion(currentFilters.region)
            districtSpinner.adapter = createBlackTextAdapter(districts)
            setSpinnerSelection(districtSpinner, currentFilters.district)
        }
    }

    private fun createBlackTextAdapter(data: List<String>): ArrayAdapter<String> {
        return object : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, data) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(android.graphics.Color.BLACK)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(android.graphics.Color.WHITE)
                return view
            }
        }.apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun setSpinnerSelection(spinner: Spinner, value: String) {
        val adapter = spinner.adapter as? ArrayAdapter<String>
        if (adapter != null) {
            val position = adapter.getPosition(value)
            if (position >= 0) {
                spinner.setSelection(position)
            }
        }
    }

    private fun setupButtons() {
        val buttonApply = findViewById<Button>(R.id.buttonApplyFilters)
        val buttonClear = findViewById<Button>(R.id.buttonClearFilters)

        buttonApply.setOnClickListener {
            // Обновляем фильтры перед применением
            currentFilters.date = dateTextView.text.toString()
            currentFilters.activityType = activityTypeSpinner.selectedItem?.toString() ?: ""
            currentFilters.workType = workTypeSpinner.selectedItem?.toString() ?: ""
            currentFilters.workPlace = workPlaceSpinner.selectedItem?.toString() ?: ""
            currentFilters.region = regionSpinner.selectedItem?.toString() ?: ""
            currentFilters.district = districtSpinner.selectedItem?.toString() ?: ""

            onFiltersApplied(currentFilters)
            dismiss()
        }

        buttonClear.setOnClickListener {
            // Очищаем поля внутри currentFilters
            currentFilters.date = ""
            currentFilters.activityType = ""
            currentFilters.workType = ""
            currentFilters.workPlace = ""
            currentFilters.region = ""
            currentFilters.district = ""

            // Обновляем отображение в интерфейсе
            dateTextView.text = ""
            activityTypeSpinner.setSelection(0)
            workTypeSpinner.setSelection(0)
            workPlaceSpinner.setSelection(0)
            regionSpinner.setSelection(0)
            districtSpinner.adapter = createBlackTextAdapter(listOf("")) // Очищаем районы

            onFiltersApplied(currentFilters)
            dismiss()
        }
    }
}
