// MapActivity.kt
package com.example.a123 // Замените на ваш действительный package name

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Metadata
import com.drew.metadata.exif.GpsDirectory
import com.drew.metadata.Tag
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private lateinit var buttonSelectPhotos: Button
    private lateinit var editTextHeight: EditText
    private lateinit var editTextSpeed: EditText
    private lateinit var buttonBuildRoute: Button
    private lateinit var buttonHistory: Button

    private val markersList = mutableListOf<LatLng>()

    companion object {
        private const val REQUEST_PERMISSIONS_CODE = 1001
        private const val TAG = "MapActivity"
    }

    // Используем современный контракт для выбора нескольких изображений
    private val selectPhotosLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            extractAndAddMarkers(uris)
        } else {
            Toast.makeText(this, "Фотографии не выбраны", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Установите макет активности
        setContentView(R.layout.activity_map)

        // Инициализация Google Maps
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Инициализация View
        buttonSelectPhotos = findViewById(R.id.buttonSelectPhotos)
        editTextHeight = findViewById(R.id.editTextHeight)
        editTextSpeed = findViewById(R.id.editTextSpeed)
        buttonBuildRoute = findViewById(R.id.buttonBuildRoute)
        buttonHistory = findViewById(R.id.buttonHistory)

        // Обработчики кнопок
        buttonSelectPhotos.setOnClickListener {
            openFilePicker()
        }

        buttonBuildRoute.setOnClickListener {
            buildRoute()
        }

        buttonHistory.setOnClickListener {
            // Реализуйте переход к истории, если необходимо
            Toast.makeText(this, "Функция Истории еще не реализована", Toast.LENGTH_SHORT).show()
        }

        // Проверка и запрос разрешений
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                getRequiredPermissions(),
                REQUEST_PERMISSIONS_CODE
            )
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Настройка типа карты (спутниковая)
        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
    }

    /**
     * Открытие файлового менеджера для выбора фотографий
     */
    private fun openFilePicker() {
        selectPhotosLauncher.launch("*/*")
    }

    /**
     * Извлечение GPS-координат из изображений и добавление их на карту
     */
    private fun extractAndAddMarkers(uris: List<Uri>) {
        val extractedMarkers = mutableListOf<LatLng>()
        lifecycleScope.launch(Dispatchers.IO) {
            for (uri in uris) {
                try {
                    val tempFile = copyUriToTempFile(uri)
                    if (tempFile != null) {
                        val geoPoint = extractGPSFromImage(tempFile)
                        if (geoPoint != null) {
                            extractedMarkers.add(geoPoint)
                        } else {
                            Log.d(TAG, "Фотография $uri не содержит GPS-данных или не удалось их извлечь")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при обработке фотографии: ${e.message}")
                }
            }
            withContext(Dispatchers.Main) {
                if (extractedMarkers.isNotEmpty()) {
                    markersList.addAll(extractedMarkers)
                    addMarkersToMap(extractedMarkers)
                    Toast.makeText(this@MapActivity, "Все метки добавлены на карту", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MapActivity, "Не удалось извлечь GPS-данные ни из одной фотографии", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Добавление списка маркеров на карту
     */
    private fun addMarkersToMap(markers: List<LatLng>) {
        for (marker in markers) {
            mMap.addMarker(
                MarkerOptions()
                    .position(marker)
                    .title("Фото")
                    .snippet("(${marker.latitude}, ${marker.longitude})")
            )
        }
        if (markers.isNotEmpty()) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markers[0], 15f))
        }
    }

    /**
     * Извлечение GPS-координат из изображения с использованием metadata-extractor
     */
    private fun extractGPSFromImage(file: File): LatLng? {
        try {
            val metadata: Metadata = ImageMetadataReader.readMetadata(file)
            val gpsDir = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)
            if (gpsDir != null) {
                val geoLocation = gpsDir.geoLocation
                if (geoLocation != null) {
                    return LatLng(geoLocation.latitude, geoLocation.longitude)
                }
            }

            // Логирование всех тегов для диагностики
            for (directory in metadata.directories) {
                for (tag in directory.tags) {
                    Log.d(TAG, "[${directory.name}] ${tag.tagName} = ${tag.description}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при использовании MetadataExtractor: ${e.message}")
        }
        return null
    }

    /**
     * Построение маршрута и экспорт в формате KML
     */
    private fun buildRoute() {
        if (markersList.size < 2) {
            Toast.makeText(this@MapActivity, "Выберите как минимум две фотографии с GPS-данными", Toast.LENGTH_SHORT).show()
            return
        }

        // Получение параметров
        val height = editTextHeight.text.toString().toDoubleOrNull() ?: 2.0
        val speed = editTextSpeed.text.toString().toDoubleOrNull() ?: 5.0

        // Создание KML
        val kml = createKML(markersList, height, speed)

        // Сохранение KML в файл
        val kmlFile = saveKMLToFile(kml)

        // Предоставление возможности экспортировать KML
        if (kmlFile != null) {
            val fileUri: Uri = FileProvider.getUriForFile(
                this@MapActivity,
                "${applicationContext.packageName}.fileprovider",
                kmlFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.google-earth.kml+xml"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "Маршрут KML")
                putExtra(Intent.EXTRA_TEXT, "Вот ваш построенный маршрут.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Экспортировать KML"))
        } else {
            Toast.makeText(this@MapActivity, "Ошибка при сохранении KML-файла", Toast.LENGTH_SHORT).show()
        }

        // Сброс состояния после экспорта
        resetState()
    }

    /**
     * Создание KML-файла из списка маркеров
     */
    private fun createKML(points: List<LatLng>, height: Double, speed: Double): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n")
        sb.append("  <Document>\n")
        sb.append("    <name>Построенный маршрут</name>\n")
        sb.append("    <description>Маршрут с высотой $height метров и скоростью $speed км/ч</description>\n")

        // Добавление каждой точки как отдельного маркера в KML
        for ((index, point) in points.withIndex()) {
            sb.append("    <Placemark>\n")
            sb.append("      <name>Точка ${index + 1}</name>\n")
            sb.append("      <Point>\n")
            sb.append("        <coordinates>${point.longitude},${point.latitude},$height</coordinates>\n")
            sb.append("      </Point>\n")
            sb.append("    </Placemark>\n")
        }

        // Добавление линии маршрута в KML
        sb.append("    <Placemark>\n")
        sb.append("      <name>Линия маршрута</name>\n")
        sb.append("      <LineString>\n")
        sb.append("        <tessellate>1</tessellate>\n")
        sb.append("        <altitudeMode>absolute</altitudeMode>\n")
        sb.append("        <coordinates>\n")
        for (point in points) {
            sb.append("${point.longitude},${point.latitude},$height\n")
        }
        sb.append("        </coordinates>\n")
        sb.append("      </LineString>\n")
        sb.append("    </Placemark>\n")
        sb.append("  </Document>\n")
        sb.append("</kml>")
        return sb.toString()
    }

    /**
     * Сохранение KML-контента в файл
     */
    private fun saveKMLToFile(kmlContent: String): File? {
        return try {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "route_$timeStamp.kml"
            val file = File(getExternalFilesDir(null), fileName)
            FileOutputStream(file).use { fos ->
                OutputStreamWriter(fos).use { writer ->
                    writer.write(kmlContent)
                }
            }
            file
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при сохранении KML: ${e.message}")
            null
        }
    }

    /**
     * Сброс состояния после экспорта
     */
    private fun resetState() {
        markersList.clear()
        mMap.clear()
        Toast.makeText(this, "Состояние сброшено, можно создать новый маршрут", Toast.LENGTH_SHORT).show()
    }

    /**
     * Проверка предоставления всех необходимых разрешений
     */
    private fun allPermissionsGranted(): Boolean {
        val permissions = getRequiredPermissions()
        return permissions.all {
            ContextCompat.checkSelfPermission(this@MapActivity, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Получение списка необходимых разрешений, учитывая версию Android
     */
    private fun getRequiredPermissions(): Array<String> {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        permissions.add(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
        )
        return permissions.toTypedArray()
    }

    /**
     * Обработка результата запроса разрешений
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (allPermissionsGranted()) {
                Toast.makeText(this, "Все разрешения предоставлены", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this@MapActivity,
                    "Необходимы все разрешения для работы приложения",
                    Toast.LENGTH_LONG
                ).show()
                // Опционально: завершить активность или ограничить функциональность
            }
        }
    }

    /**
     * Создание временного файла из Uri
     */
    private fun copyUriToTempFile(uri: Uri): File? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val tempFile = File.createTempFile("temp_image", ".jpg", cacheDir)
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                tempFile
            } ?: run {
                Log.e(TAG, "InputStream is null для URI: $uri")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при копировании URI во временный файл: ${e.message}")
            null
        }
    }
}