package com.example.a123

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate


class Weather : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var day1TextView: TextView
    private lateinit var day2TextView: TextView
    private lateinit var day3TextView: TextView
    private lateinit var day4TextView: TextView
    private lateinit var day5TextView: TextView
    private lateinit var day6TextView: TextView
    private lateinit var day7TextView: TextView
    private lateinit var locationNameTextView: TextView
    private lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_weather)

        lineChart = findViewById(R.id.lineChart)

        // Инициализация TextView для каждого дня
        day2TextView = findViewById(R.id.day2)
        day3TextView = findViewById(R.id.day3)
        day4TextView = findViewById(R.id.day4)
        day5TextView = findViewById(R.id.day5)
        day6TextView = findViewById(R.id.day6)
        day7TextView = findViewById(R.id.day7)
        locationNameTextView = findViewById(R.id.location_name)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Проверка разрешений на доступ к местоположению
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        // Получение координат и запуск процесса получения погоды
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude

                // Выполняем обратное геокодирование для получения названия населенного пункта
                getLocationName(latitude, longitude)

                // Получаем почасовой прогноз для текущего дня и ежедневный для следующих дней
                getHourlyWeatherData(latitude, longitude)
                getDailyWeatherData(latitude, longitude)
            } else {
                Log.e("WeatherApp", "Местоположение не найдено")
            }
        }.addOnFailureListener {
            Log.e("WeatherApp", "Ошибка получения местоположения: ${it.message}")
        }
    }

    // Получение названия населенного пункта по координатам
    private fun getLocationName(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                locationNameTextView.text = "Населенный пункт: ${address.locality}"
            } else {
                locationNameTextView.text = "Населенный пункт не найден"
            }
        } catch (e: IOException) {
            Log.e("WeatherApp", "Ошибка геокодирования: ${e.message}")
            locationNameTextView.text = "Ошибка геокодирования"
        }
    }

    // Получение почасовых данных для текущего дня
    private fun getHourlyWeatherData(latitude: Double, longitude: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(WeatherApiService::class.java)

        val call = api.getHourlyWeatherForecast(
            latitude = latitude,
            longitude = longitude,
            hourly = "temperature_2m,relative_humidity_2m,dewpoint_2m",
            timezone = "Europe/Moscow"
        )

        call.enqueue(object : Callback<HourlyWeatherResponse> {
            override fun onResponse(call: Call<HourlyWeatherResponse>, response: Response<HourlyWeatherResponse>) {
                if (response.isSuccessful) {
                    val weather = response.body()
                    val hourlyTemperatures = weather?.hourly?.temperature_2m ?: emptyList()
                    val hourlyHumidity = weather?.hourly?.relative_humidity_2m ?: emptyList()
                    val hourlyDewpoint = weather?.hourly?.dewpoint_2m ?: emptyList()
                    val hours = (0 until hourlyTemperatures.size).map { "${it}:00" }

                    // Отображаем график с тремя линиями
                    showHourlyForecastGraph(hourlyTemperatures, hourlyHumidity, hourlyDewpoint, hours)
                } else {
                    Log.e("WeatherApp", "Ошибка получения почасовых данных: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<HourlyWeatherResponse>, t: Throwable) {
                day1TextView.text = "Ошибка: ${t.message}"
            }
        })

    }



    private fun showHourlyForecastGraph(
        hourlyTemperatures: List<Double>,
        hourlyHumidity: List<Double>,
        hourlyDewpoint: List<Double>,
        hours: List<String>
    ) {
        val temperatureEntries = ArrayList<Entry>()
        val humidityEntries = ArrayList<Entry>()
        val dewpointEntries = ArrayList<Entry>()

        // Заполняем точки графиков для температуры, влажности и точки росы
        for (i in hourlyTemperatures.indices) {
            temperatureEntries.add(Entry(i.toFloat(), hourlyTemperatures[i].toFloat()))
        }
        for (i in hourlyHumidity.indices) {
            humidityEntries.add(Entry(i.toFloat(), hourlyHumidity[i].toFloat()))
        }
        for (i in hourlyDewpoint.indices) {
            dewpointEntries.add(Entry(i.toFloat(), hourlyDewpoint[i].toFloat()))
        }

        // Настройка графика для температуры
        val temperatureDataSet = LineDataSet(temperatureEntries, "Температура (°C)")
        temperatureDataSet.color = ColorTemplate.getHoloBlue()
        temperatureDataSet.setDrawCircles(false)
        temperatureDataSet.lineWidth = 2f

        // Настройка графика для влажности
        val humidityDataSet = LineDataSet(humidityEntries, "Влажность (%)")
        humidityDataSet.color = ColorTemplate.COLORFUL_COLORS[1]
        humidityDataSet.setDrawCircles(false)
        humidityDataSet.lineWidth = 2f

        // Настройка графика для точки росы
        val dewpointDataSet = LineDataSet(dewpointEntries, "Точка росы (°C)")
        dewpointDataSet.color = ColorTemplate.COLORFUL_COLORS[2]
        dewpointDataSet.setDrawCircles(false)
        dewpointDataSet.lineWidth = 2f

        // Добавляем все наборы данных в график
        val lineData = LineData(temperatureDataSet, humidityDataSet, dewpointDataSet)
        lineChart.data = lineData

        val hours = listOf(
            "00:00",
            "04:00",
            "08:00",
            "12:00",
            "16:00",
            "20:00"
        )

        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(hours) // Устанавливаем форматирование оси X с часами
        xAxis.granularity = 1f // Гранулярность для отображения каждой метки
        xAxis.labelCount = hours.size // Устанавливаем количество меток, равное размеру списка hours
        xAxis.position = XAxis.XAxisPosition.BOTTOM // Ось X внизу графика
        xAxis.labelRotationAngle = 0f // Поворачиваем метки на 45 градусов для лучшей читаемости
        xAxis.setDrawGridLines(false) // Отключаем сетку для оси X
        xAxis.setLabelCount(6, true) // Автоматически распределяем 6 меток равномерно
// Обновляем данные для графика
        lineChart.invalidate() // Перерисовываем график




// Обновляем данные для графика
        lineChart.invalidate() // Перерисовываем график

        // Отключаем правую ось
        lineChart.axisRight.isEnabled = true

        // Отключаем описание графика
        lineChart.description.isEnabled = true

        // Перерисовываем график
        lineChart.invalidate()
    }


    // Получение ежедневных данных для следующих дней
    private fun getDailyWeatherData(latitude: Double, longitude: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(WeatherApiService::class.java)

        // Обновленный запрос с более простыми параметрами
        val call = api.getDailyWeatherForecast(
            latitude = latitude,
            longitude = longitude,
            daily = "temperature_2m_max,precipitation_sum,shortwave_radiation_sum,wind_speed_10m_max",
            timezone = "Europe/Moscow"
        )

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weather = response.body()
                    Log.d("WeatherApp", "Ежедневные данные: ${weather}")

                    // Получаем текущую дату
                    val calendar = Calendar.getInstance()
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy EEEE", Locale("ru"))

                    // Отображение данных для каждого дня с учётом текущей даты
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    day2TextView.text = "${dateFormat.format(calendar.time)}\n" + formatDailyWeatherDataForDay(1, weather)

                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    day3TextView.text = "${dateFormat.format(calendar.time)}\n" + formatDailyWeatherDataForDay(2, weather)

                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    day4TextView.text = "${dateFormat.format(calendar.time)}\n" + formatDailyWeatherDataForDay(3, weather)

                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    day5TextView.text = "${dateFormat.format(calendar.time)}\n" + formatDailyWeatherDataForDay(4, weather)

                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    day6TextView.text = "${dateFormat.format(calendar.time)}\n" + formatDailyWeatherDataForDay(5, weather)

                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    day7TextView.text = "${dateFormat.format(calendar.time)}\n" + formatDailyWeatherDataForDay(6, weather)
                } else {
                    Log.e("WeatherApp", "Ошибка получения ежедневных данных: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                day2TextView.text = "Ошибка: ${t.message}"
            }
        })
    }


    // Форматирование почасовых данных для текущего дня
    private fun formatHourlyWeatherData(weather: HourlyWeatherResponse?): String {
        return """
            Почасовой прогноз:
            Температура: ${weather?.hourly?.temperature_2m}
            Влажность: ${weather?.hourly?.relative_humidity_2m}
            Точка росы: ${weather?.hourly?.dewpoint_2m}
        """.trimIndent()
    }

    // Форматирование ежедневных данных для каждого дня
    private fun formatDailyWeatherDataForDay(day: Int, weather: WeatherResponse?): String {
        return """
            Температура (макс.): ${weather?.daily?.temperature_2m_max?.getOrNull(day - 1)}
            Осадки: ${weather?.daily?.precipitation_sum?.getOrNull(day - 1)}
            Солнечное излучение: ${weather?.daily?.shortwave_radiation_sum?.getOrNull(day - 1)}
            Скорость ветра: ${weather?.daily?.wind_speed_10m_max?.getOrNull(day - 1)}
        """.trimIndent()
    }

    interface WeatherApiService {
        @GET("forecast")
        fun getHourlyWeatherForecast(
            @Query("latitude") latitude: Double,
            @Query("longitude") longitude: Double,
            @Query("hourly") hourly: String,
            @Query("timezone") timezone: String
        ): Call<HourlyWeatherResponse>

        @GET("forecast")
        fun getDailyWeatherForecast(
            @Query("latitude") latitude: Double,
            @Query("longitude") longitude: Double,
            @Query("daily") daily: String,
            @Query("timezone") timezone: String
        ): Call<WeatherResponse>
    }

    data class WeatherResponse(
        val daily: DailyWeather
    )

    data class HourlyWeatherResponse(
        val hourly: HourlyWeather
    )

    data class DailyWeather(
        val temperature_2m_max: List<Double>,
        val precipitation_sum: List<Double>,
        val shortwave_radiation_sum: List<Double>,
        val wind_speed_10m_max: List<Double>
    )

    data class HourlyWeather(
        val temperature_2m: List<Double>,
        val relative_humidity_2m: List<Double>,
        val dewpoint_2m: List<Double>
    )
}
