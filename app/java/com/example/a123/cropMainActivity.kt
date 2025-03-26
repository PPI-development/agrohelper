package com.example.a123

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream
import android.widget.Toast


class cropMainActivity : AppCompatActivity() {
    private lateinit var lineChart: LineChart
    private lateinit var forecastChart: LineChart
    private lateinit var pestSpinner: Spinner
    private lateinit var tvDependencyResult: TextView

    private val pestTypes = arrayOf(
        "Амбарный долгоносик", "Вредная черепашка", "Саранча", "Колорадский жук",
        "Хлопковая совка", "Септориоз", "Дубовый листовертка", "Ползучий чертополох",
        "Паутинный клещ", "Золотистая картофельная нематода", "Гессенская муха", "Бактериальный ожог",
        "Капустная моль", "Южноамериканская томатная моль", "Кукурузный мотылек", "Жук-чернотелка",
        "Щелкуны", "Полевой повелитель"
    )

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val weatherApi = retrofit.create(WeatherApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cropactivity_main)

        pestSpinner = findViewById(R.id.pestSpinner)
        lineChart = findViewById(R.id.lineChart)
        forecastChart = findViewById(R.id.forecastChart)
        tvDependencyResult = findViewById(R.id.tvDependencyResult)

        val btnShowGraph: Button = findViewById(R.id.btnShowGraph)
        val btnCalculate: Button = findViewById(R.id.btnCalculate)
        val btnPredict: Button = findViewById(R.id.btnPredict)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, pestTypes)
        pestSpinner.adapter = adapter

        btnShowGraph.setOnClickListener {
            val selectedPest = pestSpinner.selectedItem.toString()
            loadAndShowGraph(selectedPest)
        }

        btnCalculate.setOnClickListener {
            val selectedPest = pestSpinner.selectedItem.toString()
            calculateAndShowDependency(selectedPest)
        }

        btnPredict.setOnClickListener {
            val selectedPest = pestSpinner.selectedItem.toString()
            predictAndShowSpread(selectedPest)
        }
    }

    private fun loadAndShowGraph(pest: String) {
        // Загрузить соответствующий файл Excel в зависимости от выбранного вредителя
        val resourceId = getResourceIdForPest(pest)
        val pestData = DataLoader.readPestDataFromExcel(this, resourceId)
        val pestEntries = pestData.map { Entry(it.year.toFloat(), it.pestCount) }
        val temperatureEntries = pestData.map { Entry(it.year.toFloat(), it.temperature) }
        val precipitationEntries = pestData.map { Entry(it.year.toFloat(), it.precipitation) }

        // Отобразить график
        displayGraph(pestEntries, temperatureEntries, precipitationEntries)
    }

    private fun calculateAndShowDependency(pest: String) {
        val resourceId = getResourceIdForPest(pest)
        val pestData = DataLoader.readPestDataFromExcel(this, resourceId)
        val (beta0, betaTemp, betaPrec) = cropRegressionCalculator.calculateLinearRegression(pestData)

        val resultText = "Зависимость численности $pest:\n" +
                "Численность = $beta0 + $betaTemp * Температура + $betaPrec * Осадки"
        showDependencyResult(resultText)
    }

    private fun predictAndShowSpread(pest: String) {
        val latitude = 43.0
        val longitude = 76.0

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = weatherApi.getForecast(latitude, longitude)
                val forecastData = response.daily

                val resourceId = getResourceIdForPest(pest)
                val pestData = DataLoader.readPestDataFromExcel(this@cropMainActivity, resourceId)
                val (beta0, betaTemp, betaPrec) = cropRegressionCalculator.calculateLinearRegression(pestData)

                val forecastEntries = forecastData.time.mapIndexed { index, date ->
                    val temperature = forecastData.temperature_2m_max[index]
                    val precipitation = forecastData.precipitation_sum[index]
                    val predictedCount = beta0 + betaTemp * temperature + betaPrec * precipitation
                    Entry(index.toFloat(), predictedCount)
                }

                withContext(Dispatchers.Main) {
                    displayForecastGraph(forecastEntries)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getResourceIdForPest(pest: String): Int {
        return when (pest) {
            "Амбарный долгоносик" -> R.raw.breadbeetle_zko
            "Вредная черепашка" -> R.raw.tmnt_zko
            //"Саранча" -> R.raw.locusts_data  // Замените на фактический файл, если он доступен
            "Колорадский жук" -> R.raw.coloradobeetle_zko
            "Хлопковая совка" -> R.raw.sovka_to
            "Септориоз" -> R.raw.septoriosis_akm
            "Дубовый листовертка" -> R.raw.oak_alm1
            "Ползучий чертополох" -> R.raw.repens_alm
            "Паутинный клещ" -> R.raw.spider_alm
            "Золотистая картофельная нематода" -> R.raw.golden_vko
            "Гессенская муха" -> R.raw.hessianfly_kos
            "Бактериальный ожог" -> R.raw.fireblight_enb
            //"Капустная моль" -> R.raw.r_cabbagemoth_alm
            "Южноамериканская томатная моль" -> R.raw.r_satomato_alm
            "Кукурузный мотылек" -> R.raw.r_cornborer_enb
            "Жук-чернотелка" -> R.raw.r_darkbeetle_ker
            "Щелкуны" -> R.raw.r_clickbeetle_ker
            else -> R.raw.tmnt_zko // Замените на файл по умолчанию, если вредитель не найден
        }
    }

    private fun displayGraph(
        pestEntries: List<Entry>,
        temperatureEntries: List<Entry>,
        precipitationEntries: List<Entry>
    ) {
        if (pestEntries.isEmpty() || temperatureEntries.isEmpty() || precipitationEntries.isEmpty()) {
            // Обработка случая, когда данные отсутствуют или некорректны
            Toast.makeText(this, "Нет данных для отображения", Toast.LENGTH_SHORT).show()
            return
        }

        val pestDataSet = LineDataSet(pestEntries, "Численность вредителя")
        pestDataSet.color = resources.getColor(R.color.red, null)

        val tempDataSet = LineDataSet(temperatureEntries, "Температура")
        tempDataSet.color = resources.getColor(R.color.blue, null)

        val precDataSet = LineDataSet(precipitationEntries, "Осадки")
        precDataSet.color = resources.getColor(R.color.green, null)

        val lineData = LineData(pestDataSet, tempDataSet, precDataSet)
        lineChart.data = lineData
        lineChart.invalidate()
    }


    private fun displayForecastGraph(forecastEntries: List<Entry>) {
        if (forecastEntries.isEmpty()) {
            // Обработка случая, когда прогнозные данные отсутствуют или некорректны
            Toast.makeText(this, "Нет данных для прогнозирования", Toast.LENGTH_SHORT).show()
            return
        }

        val forecastDataSet = LineDataSet(forecastEntries, "Прогноз численности")
        forecastDataSet.color = resources.getColor(R.color.green, null)

        val lineData = LineData(forecastDataSet)
        forecastChart.data = lineData
        forecastChart.invalidate()
    }


    private fun showDependencyResult(dependency: String) {
        tvDependencyResult.text = dependency
    }
}

