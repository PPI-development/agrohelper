package com.example.a123

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class ProgMainActivity : AppCompatActivity() {

    private lateinit var xmlHelper: FieldXmlHelper
    private val REQUEST_CODE_ADD_FIELD = 100

    // Ваши данные для расчета
    private val cropData = mapOf(
        "Пшеница" to 8.0,
        "Кукуруза" to 12.0,
        "Подсолнечник" to 4.0,
        "Соя" to 3.0,
        "Картофель" to 40.0,
        "Рапс" to 3.5
    )

    private val pesticides = mapOf(
        "Пшеница" to listOf("Нет" to 1.0, "Глифосат" to 0.9, "2,4-Д" to 0.95, "Дикамба" to 0.85, "Метрибузин" to 0.9, "Трифлусульфурон" to 0.8),
        "Кукуруза" to listOf("Нет" to 1.0, "Ацетохлор" to 0.9, "Метолахлор" to 0.85, "Атразин" to 0.8, "Никосульфурон" to 0.9, "Примекстра" to 0.85),
        "Подсолнечник" to listOf("Нет" to 1.0, "Трифлуралин" to 0.85, "Прометрин" to 0.8, "Имазамокс" to 0.9, "Хизалофоп-П" to 0.95, "Квинклорак" to 0.9),
        "Соя" to listOf("Нет" to 1.0, "Имидаклоприд" to 0.85, "Глифосат" to 0.9, "Метрибузин" to 0.8, "Клетодим" to 0.9, "Флуазифоп-П" to 0.95),
        "Картофель" to listOf("Нет" to 1.0, "Хлорпирифос" to 0.9, "Имазамокс" to 0.85, "Метрибузин" to 0.8, "Линурон" to 0.9, "Прометрин" to 0.95),
        "Рапс" to listOf("Нет" to 1.0, "Метазахлор" to 0.85, "Клетодим" to 0.9, "Пропизохлор" to 0.8, "Напропамид" to 0.9, "Квинклорак" to 0.95)
    )


    private val fertilizers = mapOf(
        "Пшеница" to listOf("Нет" to 1.0, "Азот (N)" to 1.1, "Фосфор (P)" to 1.2, "Калий (K)" to 1.15, "Сера (S)" to 1.1, "Цинк (Zn)" to 1.05),
        "Кукуруза" to listOf("Нет" to 1.0, "Азот (N)" to 1.15, "Фосфор (P)" to 1.1, "Калий (K)" to 1.2, "Магний (Mg)" to 1.1, "Бор (B)" to 1.05),
        "Подсолнечник" to listOf("Нет" to 1.0, "Фосфор (P)" to 1.1, "Калий (K)" to 1.05, "Сера (S)" to 1.1, "Бор (B)" to 1.05, "Магний (Mg)" to 1.1),
        "Соя" to listOf("Нет" to 1.0, "Калий (K)" to 1.1, "Фосфор (P)" to 1.05, "Сера (S)" to 1.1, "Кальций (Ca)" to 1.05, "Молибден (Mo)" to 1.1),
        "Картофель" to listOf("Нет" to 1.0, "Фосфор (P)" to 1.1, "Калий (K)" to 1.15, "Магний (Mg)" to 1.1, "Бор (B)" to 1.05, "Медь (Cu)" to 1.05),
        "Рапс" to listOf("Нет" to 1.0, "Азот (N)" to 1.1, "Калий (K)" to 1.15, "Сера (S)" to 1.1, "Бор (B)" to 1.05, "Магний (Mg)" to 1.05)
    )


    private val humus = mapOf(
        "Пшеница" to listOf("Нет" to 1.0, "Высокое содержание" to 1.1, "Среднее содержание" to 1.05, "Низкое содержание" to 1.0),
        "Кукуруза" to listOf("Нет" to 1.0, "Высокое содержание" to 1.1, "Среднее содержание" to 1.05, "Низкое содержание" to 1.0),
        "Подсолнечник" to listOf("Нет" to 1.0, "Высокое содержание" to 1.15, "Среднее содержание" to 1.1, "Низкое содержание" to 1.05),
        "Соя" to listOf("Нет" to 1.0, "Высокое содержание" to 1.1, "Среднее содержание" to 1.05, "Низкое содержание" to 1.0),
        "Картофель" to listOf("Нет" to 1.0, "Высокое содержание" to 1.2, "Среднее содержание" to 1.15, "Низкое содержание" to 1.1),
        "Рапс" to listOf("Нет" to 1.0, "Высокое содержание" to 1.15, "Среднее содержание" to 1.1, "Низкое содержание" to 1.05)
    )


    private val topsoil = mapOf(
        "Пшеница" to listOf("Нет" to 1.0, "Толщина 10 см" to 1.05, "Толщина 20 см" to 1.1, "Толщина 30 см" to 1.15),
        "Кукуруза" to listOf("Нет" to 1.0, "Толщина 10 см" to 1.05, "Толщина 20 см" to 1.1, "Толщина 30 см" to 1.15),
        "Подсолнечник" to listOf("Нет" to 1.0, "Толщина 10 см" to 1.1, "Толщина 20 см" to 1.15, "Толщина 30 см" to 1.2),
        "Соя" to listOf("Нет" to 1.0, "Толщина 10 см" to 1.1, "Толщина 20 см" to 1.15, "Толщина 30 см" to 1.2),
        "Картофель" to listOf("Нет" to 1.0, "Толщина 10 см" to 1.15, "Толщина 20 см" to 1.2, "Толщина 30 см" to 1.25),
        "Рапс" to listOf("Нет" to 1.0, "Толщина 10 см" to 1.1, "Толщина 20 см" to 1.15, "Толщина 30 см" to 1.2)
    )


    private val soilTypes = mapOf(
        "Пшеница" to listOf("Нет" to 1.0, "Чернозем" to 1.2, "Суглинок" to 0.9, "Песчаная почва" to 0.8),
        "Кукуруза" to listOf("Нет" to 1.0, "Чернозем" to 1.25, "Суглинок" to 0.95, "Песчаная почва" to 0.85),
        "Подсолнечник" to listOf("Нет" to 1.0, "Чернозем" to 1.15, "Суглинок" to 0.9, "Песчаная почва" to 0.8),
        "Соя" to listOf("Нет" to 1.0, "Чернозем" to 1.2, "Суглинок" to 0.95, "Песчаная почва" to 0.85),
        "Картофель" to listOf("Нет" to 1.0, "Чернозем" to 1.3, "Суглинок" to 1.0, "Песчаная почва" to 0.9),
        "Рапс" to listOf("Нет" to 1.0, "Чернозем" to 1.2, "Суглинок" to 0.95, "Песчаная почва" to 0.85)
    )


    private val pests = mapOf(
        "Пшеница" to listOf("Нет" to 1.0, "Злаковая тля" to 0.85, "Пшеничный трипс" to 0.8, "Мучнистый клещ" to 0.75),
        "Кукуруза" to listOf("Нет" to 1.0, "Кукурузный мотылек" to 0.9, "Листовая совка" to 0.85, "Злаковая тля" to 0.8),
        "Подсолнечник" to listOf("Нет" to 1.0, "Подсолнечная моль" to 0.8, "Листовая совка" to 0.75, "Злаковая тля" to 0.7),
        "Соя" to listOf("Нет" to 1.0, "Соевая тля" to 0.85, "Луговой мотылек" to 0.8, "Плодожорка" to 0.75),
        "Картофель" to listOf("Нет" to 1.0, "Колорадский жук" to 0.7, "Картофельная моль" to 0.75, "Листовая совка" to 0.8),
        "Рапс" to listOf("Нет" to 1.0, "Крестоцветная блошка" to 0.85, "Рапсовый цветоед" to 0.8, "Луговой мотылек" to 0.75)
    )


    private val diseases = mapOf(
        "Пшеница" to listOf("Нет" to 1.0, "Мучнистая роса" to 0.85, "Бурая ржавчина" to 0.8, "Фузариоз" to 0.75),
        "Кукуруза" to listOf("Нет" to 1.0, "Фузариоз" to 0.8, "Пузырчатая головня" to 0.75, "Листовая пятнистость" to 0.7),
        "Подсолнечник" to listOf("Нет" to 1.0, "Белая гниль" to 0.8, "Фомоз" to 0.75, "Альтернариоз" to 0.7),
        "Соя" to listOf("Нет" to 1.0, "Пероноспороз" to 0.85, "Аскохитоз" to 0.8, "Фузариоз" to 0.75),
        "Картофель" to listOf("Нет" to 1.0, "Фитофтороз" to 0.8, "Парша обыкновенная" to 0.75, "Ризоктониоз" to 0.7),
        "Рапс" to listOf("Нет" to 1.0, "Пероноспороз" to 0.85, "Фомоз" to 0.8, "Склеротиния" to 0.75)
    )


    private val weeds = mapOf(
        "Пшеница" to listOf("Нет" to 1.0, "Пырей ползучий" to 0.85, "Осот полевой" to 0.8, "Мышейник" to 0.75),
        "Кукуруза" to listOf("Нет" to 1.0, "Палмер амарант" to 0.85, "Щирица" to 0.8, "Осот полевой" to 0.75),
        "Подсолнечник" to listOf("Нет" to 1.0, "Вьюнок полевой" to 0.8, "Амброзия" to 0.75, "Осот полевой" to 0.7),
        "Соя" to listOf("Нет" to 1.0, "Щирица" to 0.8, "Пырей ползучий" to 0.75, "Амброзия" to 0.7),
        "Картофель" to listOf("Нет" to 1.0, "Осот полевой" to 0.85, "Щирица" to 0.8, "Пырей ползучий" to 0.75),
        "Рапс" to listOf("Нет" to 1.0, "Палмер амарант" to 0.85, "Мышейник" to 0.8, "Щирица" to 0.75)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.progactivity_main)

        xmlHelper = FieldXmlHelper(this)

        // Пример использования данных при выборе культуры и расчете урожайности
        val spinnerCrop: Spinner = findViewById(R.id.spinner_crop)
        val spinnerField: Spinner = findViewById(R.id.spinner_field)
        val editTextArea: EditText = findViewById(R.id.editText_area)
        val buttonCalculate: Button = findViewById(R.id.button_calculate)
        val barChart: BarChart = findViewById(R.id.barChart)

        // Обновление списка полей
        updateFieldList()

        // Настройка адаптера для выбора культуры
        val crops = cropData.keys.toList()
        spinnerCrop.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, crops)

        // Обработка события нажатия на кнопку "Рассчитать"
        buttonCalculate.setOnClickListener {
            val selectedField = spinnerField.selectedItem.toString()
            val area = if (selectedField == "Другое") {
                editTextArea.text.toString().toDoubleOrNull() ?: 1.0
            } else {
                getFieldArea(selectedField)
            }
            calculateAndDisplayYield(area)
        }

        // Обработка выбора поля
        spinnerField.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedField = spinnerField.selectedItem.toString()

                // Проверяем, выбрано ли "Другое", чтобы отобразить поле ввода
                if (selectedField == "Другое") {
                    editTextArea.visibility = View.VISIBLE
                    editTextArea.hint = "Введите площадь в Га"
                } else {
                    editTextArea.visibility = View.GONE

                    // Отображаем информацию о площади для выбранного поля
                    val area = getFieldArea(selectedField)
                    findViewById<TextView>(R.id.textView_area_info).text = "Площадь: $area Га"
                }

                // Получаем выбранную культуру из спинера культур
                val selectedCrop = spinnerCrop.selectedItem.toString()

                // Обновляем спинеры для выбранной культуры
                updatePesticidesSpinner(selectedCrop)
                updateFertilizersSpinner(selectedCrop)
                updateHumusSpinner(selectedCrop)
                updateTopsoilSpinner(selectedCrop)
                updateSoilTypeSpinner(selectedCrop)
                updatePestsSpinner(selectedCrop)
                updateDiseasesSpinner(selectedCrop)
                updateWeedsSpinner(selectedCrop)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

    }

    private fun updatePesticidesSpinner(crop: String) {
        val spinnerPesticides: Spinner = findViewById(R.id.spinner_pesticides)
        val pesticideOptions = pesticides[crop]?.map { it.first } ?: listOf()
        spinnerPesticides.adapter = createBlackTextAdapter(pesticideOptions)
    }

    private fun updateFertilizersSpinner(crop: String) {
        val spinnerFertilizers: Spinner = findViewById(R.id.spinner_fertilizers)
        val fertilizerOptions = fertilizers[crop]?.map { it.first } ?: listOf()
        spinnerFertilizers.adapter = createBlackTextAdapter(fertilizerOptions)
    }

    private fun updateHumusSpinner(crop: String) {
        val spinnerHumus: Spinner = findViewById(R.id.spinner_humus)
        val humusOptions = humus[crop]?.map { it.first } ?: listOf()
        spinnerHumus.adapter = createBlackTextAdapter(humusOptions)
    }

    private fun updateTopsoilSpinner(crop: String) {
        val spinnerTopsoil: Spinner = findViewById(R.id.spinner_topsoil)
        val topsoilOptions = topsoil[crop]?.map { it.first } ?: listOf()
        spinnerTopsoil.adapter = createBlackTextAdapter(topsoilOptions)
    }

    private fun updateSoilTypeSpinner(crop: String) {
        val spinnerSoilType: Spinner = findViewById(R.id.spinner_soil_type)
        val soilTypeOptions = soilTypes[crop]?.map { it.first } ?: listOf()
        spinnerSoilType.adapter = createBlackTextAdapter(soilTypeOptions)
    }

    private fun updatePestsSpinner(crop: String) {
        val spinnerPests: Spinner = findViewById(R.id.spinner_pests)
        val pestsOptions = pests[crop]?.map { it.first } ?: listOf()
        spinnerPests.adapter = createBlackTextAdapter(pestsOptions)
    }

    private fun updateDiseasesSpinner(crop: String) {
        val spinnerDiseases: Spinner = findViewById(R.id.spinner_diseases)
        val diseasesOptions = diseases[crop]?.map { it.first } ?: listOf()
        spinnerDiseases.adapter = createBlackTextAdapter(diseasesOptions)
    }

    private fun updateWeedsSpinner(crop: String) {
        val spinnerWeeds: Spinner = findViewById(R.id.spinner_weeds)
        val weedsOptions = weeds[crop]?.map { it.first } ?: listOf()
        spinnerWeeds.adapter = createBlackTextAdapter(weedsOptions)
    }


    // Обновляем список полей
    private fun updateFieldList() {
        val fields = xmlHelper.loadFields()
        val fieldNames = fields.map { it.name } + "Другое"
        val spinnerField: Spinner = findViewById(R.id.spinner_field)
        spinnerField.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fieldNames)
    }

    // Получаем площадь поля
    private fun getFieldArea(fieldName: String): Double {
        val fields = xmlHelper.loadFields()
        val field = fields.find { it.name == fieldName }
        return field?.area ?: 1.0 // Возвращаем 1.0, если площадь не найдена
    }

    // Рассчитываем и отображаем график урожайности
    private fun calculateAndDisplayYield(area: Double) {
        val spinnerCrop: Spinner = findViewById(R.id.spinner_crop)
        val spinnerPesticides: Spinner = findViewById(R.id.spinner_pesticides)
        val spinnerFertilizers: Spinner = findViewById(R.id.spinner_fertilizers)
        val spinnerHumus: Spinner = findViewById(R.id.spinner_humus)
        val spinnerTopsoil: Spinner = findViewById(R.id.spinner_topsoil)
        val spinnerSoilType: Spinner = findViewById(R.id.spinner_soil_type)
        val spinnerPests: Spinner = findViewById(R.id.spinner_pests)
        val spinnerDiseases: Spinner = findViewById(R.id.spinner_diseases)
        val spinnerWeeds: Spinner = findViewById(R.id.spinner_weeds)
        val barChart: BarChart = findViewById(R.id.barChart)

        val selectedCrop = spinnerCrop.selectedItem.toString()
        val dvu = cropData[selectedCrop] ?: 0.0

        // Получаем выбранные значения из спинеров
        val pesticideCoeff = pesticides[selectedCrop]?.find { it.first == spinnerPesticides.selectedItem }?.second ?: 1.0
        val fertilizerCoeff = fertilizers[selectedCrop]?.find { it.first == spinnerFertilizers.selectedItem }?.second ?: 1.0
        val humusCoeff = humus[selectedCrop]?.find { it.first == spinnerHumus.selectedItem }?.second ?: 1.0
        val topsoilCoeff = topsoil[selectedCrop]?.find { it.first == spinnerTopsoil.selectedItem }?.second ?: 1.0
        val soilTypeCoeff = soilTypes[selectedCrop]?.find { it.first == spinnerSoilType.selectedItem }?.second ?: 1.0
        val pestCoeff = pests[selectedCrop]?.find { it.first == spinnerPests.selectedItem }?.second ?: 1.0
        val diseaseCoeff = diseases[selectedCrop]?.find { it.first == spinnerDiseases.selectedItem }?.second ?: 1.0
        val weedCoeff = weeds[selectedCrop]?.find { it.first == spinnerWeeds.selectedItem }?.second ?: 1.0

        // Вычисляем ожидаемый и максимальный урожай
        val expectedYield = dvu * area * pesticideCoeff * fertilizerCoeff * humusCoeff * topsoilCoeff * soilTypeCoeff * pestCoeff * diseaseCoeff * weedCoeff
        val maxYield = dvu * area

        // Создаем данные для столбцов
        val barEntries = listOf(
            BarEntry(0f, maxYield.toFloat()),  // Потенциальный урожай
            BarEntry(1f, expectedYield.toFloat())  // Ожидаемый урожай
        )

        // Настройка графика
        val barDataSet = BarDataSet(barEntries, "Урожайность")
        barDataSet.colors = listOf(Color.BLUE, Color.GREEN)
        barDataSet.valueTextColor = Color.BLACK
        barDataSet.valueTextSize = 12f
        val barData = BarData(barDataSet)

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(arrayOf("Потенциальный урожай", "Ожидаемый урожай"))
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.textColor = Color.BLACK
        xAxis.textSize = 12f

        val legend = barChart.legend
        legend.isEnabled = true
        legend.textColor = Color.BLACK
        legend.textSize = 12f
        legend.form = com.github.mikephil.charting.components.Legend.LegendForm.SQUARE
        legend.formSize = 10f

        barChart.data = barData
        barChart.description.text = "Урожайность (тонн/Га)"
        barChart.description.textColor = Color.BLACK
        barChart.invalidate()  // Обновляем график
    }

    private fun createBlackTextAdapter(data: List<String>): ArrayAdapter<String> {
        return object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.BLACK)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(Color.WHITE)
                return view
            }
        }
    }
}
