package com.example.a123

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.ceil

class CalculatorActivity : AppCompatActivity() {

    // Компоненты для готовых смесей
    private lateinit var mixSpinner: Spinner
    private lateinit var areaEditText: EditText
    private lateinit var tankSizeEditText: EditText
    private lateinit var calculationMode: RadioGroup
    private lateinit var calculateButton: Button
    private lateinit var resultTextView: TextView

    // Компоненты для собственной смеси
    private lateinit var customTankVolumeEditText: EditText
    private lateinit var addIngredientButton: Button
    private lateinit var ingredientListView: ListView
    private lateinit var calculateCustomButton: Button
    private lateinit var customResultTextView: TextView

    private lateinit var tankMixes: List<TankMix>
    private val ingredients = mutableListOf<ActiveIngredient>()
    private lateinit var ingredientAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        // Инициализация компонентов для готовых смесей
        mixSpinner = findViewById(R.id.mixSpinner)
        areaEditText = findViewById(R.id.areaEditText)
        tankSizeEditText = findViewById(R.id.tankSizeEditText)
        calculationMode = findViewById(R.id.calculationMode)
        calculateButton = findViewById(R.id.calculateButton)
        resultTextView = findViewById(R.id.resultTextView)

        // Инициализация компонентов для собственной смеси
        customTankVolumeEditText = findViewById(R.id.customTankVolumeEditText)
        addIngredientButton = findViewById(R.id.addIngredientButton)
        ingredientListView = findViewById(R.id.ingredientListView)
        calculateCustomButton = findViewById(R.id.calculateCustomButton)
        customResultTextView = findViewById(R.id.customResultTextView)

        // Загрузка данных смесей
        loadTankMixes()

        // Настройка Spinner для готовых смесей
        val mixAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            tankMixes.map { it.name }
        )
        mixAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mixSpinner.adapter = mixAdapter

        // Обработчик переключения режима расчета
        calculationMode.setOnCheckedChangeListener { _, checkedId ->
            tankSizeEditText.visibility = if (checkedId == R.id.mode_area_only) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        // Обработчик кнопки расчета для готовых смесей
        calculateButton.setOnClickListener {
            calculate()
        }

        // Настройка адаптера для списка ингредиентов
        ingredientAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        ingredientListView.adapter = ingredientAdapter

        // Обработчик кнопки добавления ингредиента
        addIngredientButton.setOnClickListener {
            showAddIngredientDialog()
        }

        // Обработчик кнопки расчета для собственной смеси
        calculateCustomButton.setOnClickListener {
            calculateCustomMix()
        }
    }

    private fun loadTankMixes() {
        tankMixes = listOf(
            TankMix("Herbicide Mix A", 50, 20, "30% Water"),
            TankMix("Insecticide Mix B", 60, 0, "40% Water"),
            TankMix("Fungicide Mix C", 45, 10, "45% Water"),
            TankMix("Herbicide/Insecticide Mix D", 55, 0, "45% Water"),
            TankMix("Fungicide/Nutrient Mix E", 40, 30, "30% Water"),
            TankMix("Oil-Based Insecticide F", 60, 0, "40% Oil"),
            TankMix("Diesel-Pesticide Mix G", 45, 10, "45% Diesel"),
            TankMix("Water-Based Fertilizer H", 35, 40, "25% Water"),
            TankMix("Multi-Purpose Mix I", 50, 20, "30% Water"),
            TankMix("Diesel-Herbicide Mix J", 55, 0, "45% Diesel")
        )
    }

    private fun calculate() {
        val selectedMixName = mixSpinner.selectedItem as String
        val selectedMix = tankMixes.find { it.name == selectedMixName }

        if (selectedMix == null) {
            Toast.makeText(this, "Выберите смесь", Toast.LENGTH_SHORT).show()
            return
        }

        val area = areaEditText.text.toString().toDoubleOrNull()
        if (area == null) {
            Toast.makeText(this, "Введите корректную площадь", Toast.LENGTH_SHORT).show()
            return
        }

        val isAreaOnly = calculationMode.checkedRadioButtonId == R.id.mode_area_only

        val tankSize = if (!isAreaOnly) {
            tankSizeEditText.text.toString().toDoubleOrNull()
        } else {
            null
        }

        if (!isAreaOnly && tankSize == null) {
            Toast.makeText(this, "Введите корректный объем бака", Toast.LENGTH_SHORT).show()
            return
        }

        // Выполнение расчета
        val result = performCalculation(selectedMix, area, isAreaOnly, tankSize)
        resultTextView.text = result
    }

    private fun performCalculation(
        mix: TankMix,
        area: Double,
        isAreaOnly: Boolean,
        tankSize: Double?
    ): String {
        // Предположим, что для расчета нам нужно количество вещества на гектар
        val substancePerHectare = 1.0 // Замените на реальные данные
        val totalSubstance = substancePerHectare * area

        return if (isAreaOnly) {
            "Вы должны использовать %.2f кг вещества и воды по рецептуре %s на площадь %.2f га."
                .format(totalSubstance, mix.base, area)
        } else {
            val tanksNeeded = ceil((area * substancePerHectare) / tankSize!!)
            val substancePerTank = totalSubstance / tanksNeeded
            "Для обработки %.2f га вам понадобится %.0f бак(ов) объемом %.2f л. В каждый бак залейте %.2f кг вещества и воду по рецептуре %s."
                .format(area, tanksNeeded, tankSize, substancePerTank, mix.base)
        }
    }

    private fun showAddIngredientDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Добавить ДВ")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val nameInput = EditText(this)
        nameInput.hint = "Название вещества"
        layout.addView(nameInput)

        val percentageInput = EditText(this)
        percentageInput.hint = "Процент (%)"
        percentageInput.inputType = InputType.TYPE_CLASS_NUMBER
        layout.addView(percentageInput)

        builder.setView(layout)

        builder.setPositiveButton("Добавить") { _, _ ->
            val name = nameInput.text.toString()
            val percentage = percentageInput.text.toString().toIntOrNull()

            if (name.isEmpty() || percentage == null) {
                Toast.makeText(this, "Введите корректные данные", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (getTotalPercentage() + percentage > 100) {
                Toast.makeText(this, "Общий процент не может превышать 100%", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val ingredient = ActiveIngredient(name, percentage)
            ingredients.add(ingredient)
            ingredientAdapter.add("${ingredient.name} - ${ingredient.percentage}%")
        }

        builder.setNegativeButton("Отмена", null)
        builder.show()
    }

    private fun getTotalPercentage(): Int {
        return ingredients.sumOf { it.percentage }
    }

    private fun calculateCustomMix() {
        val tankVolume = customTankVolumeEditText.text.toString().toDoubleOrNull()
        if (tankVolume == null) {
            Toast.makeText(this, "Введите корректный объем бака", Toast.LENGTH_SHORT).show()
            return
        }

        if (getTotalPercentage() != 100) {
            Toast.makeText(this, "Общий процент веществ должен быть равен 100%", Toast.LENGTH_SHORT).show()
            return
        }

        val resultBuilder = StringBuilder()
        for (ingredient in ingredients) {
            val volume = tankVolume * ingredient.percentage / 100
            resultBuilder.append("${ingredient.name}: %.2f л\n".format(volume))
        }

        customResultTextView.text = resultBuilder.toString()
    }
}
