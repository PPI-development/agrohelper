package com.example.a123

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.MapView

class MainActivity : AppCompatActivity(), OnMapReadyCallback {  // Реализуем OnMapReadyCallback

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    private var x1: Float = 0.0f
    private var x2: Float = 0.0f
    private var y1: Float = 0.0f
    private var y2: Float = 0.0f
    private val minDistance = 150  // Минимальное расстояние для свайпа

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)  // Передаем this, так как MainActivity реализует OnMapReadyCallback

        // Найдем основной layout и установим ему onTouchListener
        val mainLayout: View = findViewById(R.id.main_layout)  // Убедитесь, что у вас есть элемент с таким id
        mainLayout.setOnTouchListener { v, event -> handleTouch(event) }

        // Найдем логотипы по их ID и установим обработчики нажатий
        val logoImageView: ImageView = findViewById(R.id.logo)
        logoImageView.setOnClickListener {
            val websiteUrl = "https://www.niizkr.kz/"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(websiteUrl)
            startActivity(intent)
        }

        val logoImageView1: ImageView = findViewById(R.id.logo1)
        logoImageView1.setOnClickListener {
            val websiteUrl = "https://www.instagram.com/kazniizikr/?hl=en"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(websiteUrl)
            startActivity(intent)
        }

        val logoImageView2: ImageView = findViewById(R.id.logo2)
        logoImageView2.setOnClickListener {
            val websiteUrl = "https://wa.me/77712558278/"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(websiteUrl)
            startActivity(intent)
        }

        // Инициализация кнопок
        val plantyBotButton: Button = findViewById(R.id.planty_bot_button)
        val programmingButton: Button = findViewById(R.id.programming_button)
        val forecastButton: Button = findViewById(R.id.forecast_button)
        val processingButton: Button = findViewById(R.id.processing_button)
        val startWorkButton: Button = findViewById(R.id.start_work_button)

        plantyBotButton.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        programmingButton.setOnClickListener {
            val intent = Intent(this, progmain::class.java)
            startActivity(intent)
        }

        forecastButton.setOnClickListener {
            val intent = Intent(this, cropMainActivity::class.java)
            startActivity(intent)
        }

        processingButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        startWorkButton.setOnClickListener {
            val intent = Intent(this, ReportListActivity::class.java)
            startActivity(intent)
        }
    }

    // Обработка касаний для свайпов
    private fun handleTouch(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                x1 = event.x
                y1 = event.y
            }
            MotionEvent.ACTION_UP -> {
                x2 = event.x
                y2 = event.y
                val deltaX = x2 - x1
                val deltaY = y2 - y1

                if (abs(deltaX) > abs(deltaY)) {
                    // Горизонтальный свайп
                    if (abs(deltaX) > minDistance) {
                        if (x2 > x1) {
                            // Свайп вправо
                            openWeatherActivity()
                        } else {
                            // Свайп влево
                            openAnotherActivity()
                        }
                    }
                }
            }
        }
        return true
    }

    // Метод для перехода на активность с прогнозом (свайп вправо)
    private fun openWeatherActivity() {
        val intent = Intent(this, Weather::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    // Метод для перехода на другую активность (свайп влево)
    private fun openAnotherActivity() {
        val intent = Intent(this, News::class.java)
        startActivity(intent)

        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    // Реализация метода onMapReady для работы с картой
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Координаты, где будет маркер
        val coordinates = LatLng(43.184089, 76.863794) // Пример: Алматы
        googleMap.addMarker(MarkerOptions().position(coordinates).title("ТОО КАЗНИИЗИКР им. Ж. Жиембаева"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15f))

        // Установка нажатия на маркер
        googleMap.setOnMarkerClickListener {
            openNavigationApp()  // Вызываем метод для открытия URL
            true
        }
    }

    // Метод для открытия навигационного приложения
    private fun openNavigationApp() {
        // Открываем ссылку напрямую
        val gmmIntentUri = Uri.parse("https://maps.app.goo.gl/UYTQLdm83bBKCgih9")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        startActivity(mapIntent)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
