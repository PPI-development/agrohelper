// MapActivity1.kt
package com.example.a123

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.io.File
import android.util.Log
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class MapActivity1 : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private const val DEFAULT_ZOOM = 15f
    }

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private val markerReportMap: MutableMap<Marker, Report> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map1)

        // Инициализируем кнопки
        val buttonCreateReport = findViewById<Button>(R.id.buttonCreateReport2)
        val buttonFilters = findViewById<Button>(R.id.buttonFilters2)
        val buttonShowAsList = findViewById<Button>(R.id.buttonShowAsList2)
        val buttonExportReports = findViewById<Button>(R.id.buttonExportReports2)

        // Назначаем обработчики кликов для кнопок
        buttonCreateReport.setOnClickListener {
            val intent = Intent(this, CreateReportActivity::class.java)
            startActivity(intent)
        }

        buttonFilters.setOnClickListener {
            openFiltersDialog()
        }

        buttonShowAsList.setOnClickListener {
            val intent = Intent(this, ReportListActivity::class.java)
            startActivity(intent)
        }

        buttonExportReports.setOnClickListener {
            exportReports()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Проверяем доступность Google Play Services
        if (isGooglePlayServicesAvailable()) {
            if (checkLocationPermission()) {
                initMap()
            } else {
                requestLocationPermission()
            }
        } else {
            Toast.makeText(
                this,
                "Google Play Services недоступны. Пожалуйста, установите их.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val status = com.google.android.gms.common.GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(this)
        if (status != com.google.android.gms.common.ConnectionResult.SUCCESS) {
            if (com.google.android.gms.common.GoogleApiAvailability.getInstance()
                    .isUserResolvableError(status)
            ) {
                com.google.android.gms.common.GoogleApiAvailability.getInstance()
                    .getErrorDialog(this, status, 2404)?.show()
            }
            return false
        }
        return true
    }

    private fun checkLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation && coarseLocation
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_LOCATION_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if ((grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED })) {
                    initMap()
                } else {
                    Toast.makeText(
                        this,
                        "Разрешение на доступ к местоположению необходимо для работы карты.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
        }
    }

    private fun initMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment1) as? SupportMapFragment

        if (mapFragment != null) {
            mapFragment.getMapAsync(this)
        } else {
            Toast.makeText(this, "Не удалось найти SupportMapFragment.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Устанавливаем тип карты на спутниковый
        mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE

        // Включаем кнопку определения местоположения, если разрешения предоставлены
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        }

        // Получаем отчёты и отображаем их на карте
        loadReportsAndDisplayMarkers()

        // Устанавливаем слушатель кликов на маркерах
        mMap.setOnMarkerClickListener(this)
    }

    private fun loadReportsAndDisplayMarkers() {
        val reportList = ReportUtils.loadReports(this)

        for (report in reportList) {
            if (report.latitude != null && report.longitude != null) {
                val position = LatLng(report.latitude, report.longitude)
                val iconResource = getIconForActivityType(report.activityType)
                val icon = getIconForActivityType(report.activityType)
                val marker = mMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(report.workType)
                        .icon(icon)
                )
                if (marker != null) {
                    markerReportMap[marker] = report
                }
            }
        }

        if (reportList.isNotEmpty()) {
            val firstReport = reportList[0]
            if (firstReport.latitude != null && firstReport.longitude != null) {
                val firstPosition = LatLng(firstReport.latitude, firstReport.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstPosition, DEFAULT_ZOOM))
            }
        } else {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                    if (location != null) {
                        lastLocation = location
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM))
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
                Toast.makeText(this, "Ошибка доступа к местоположению.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getIconForActivityType(activityType: String): BitmapDescriptor {
        val resourceId = when (activityType) {
            "Энтомолог" -> R.drawable.icon_entomologist
            "Фитопатолог" -> R.drawable.icon_phytopathologist
            "Герболог" -> R.drawable.icon_herbologist
            else -> R.drawable.icon_default
        }

        // Загружаем оригинальное изображение и изменяем его размер
        val originalBitmap = BitmapFactory.decodeResource(resources, resourceId)
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 80, 80, false)

        return BitmapDescriptorFactory.fromBitmap(scaledBitmap)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val report = markerReportMap[marker]
        if (report != null) {
            AlertDialog.Builder(this)
                .setTitle(report.workType)
                .setMessage("Что вы хотите сделать с этим отчётом?")
                .setPositiveButton("Просмотреть") { dialog, which ->
                    openReportForViewing(report)
                }
                .setNegativeButton("Удалить") { dialog, which ->
                    showDeleteConfirmationDialog(report)
                }
                .show()
            return true
        }
        return false
    }

    private fun openReportForViewing(report: Report) {
        val intent = Intent(this, ReportViewActivity::class.java)
        intent.putExtra("xmlFilePath", report.xmlFilePath)
        startActivity(intent)
    }

    private fun showDeleteConfirmationDialog(report: Report) {
        AlertDialog.Builder(this)
            .setTitle("Удалить отчёт")
            .setMessage("Вы уверены, что хотите удалить этот отчёт?")
            .setPositiveButton("Да") { dialog, which ->
                deleteReport(report)
            }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun deleteReport(report: Report) {
        val file = File(report.xmlFilePath)
        Log.d("MapActivity1", "Пытаемся удалить файл: ${file.absolutePath}")
        if (file.exists()) {
            if (file.delete()) {
                Log.d("MapActivity1", "Файл успешно удалён: ${file.absolutePath}")
                removeMarkerForReport(report)
                Toast.makeText(this, "Отчёт удалён.", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("MapActivity1", "Не удалось удалить файл: ${file.absolutePath}")
                Toast.makeText(this, "Не удалось удалить отчёт.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("MapActivity1", "Файл не найден: ${file.absolutePath}")
            Toast.makeText(this, "Файл отчёта не найден.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeMarkerForReport(report: Report) {
        val markerToRemove = markerReportMap.filter { it.value == report }.keys.firstOrNull()
        if (markerToRemove != null) {
            markerToRemove.remove()
            markerReportMap.remove(markerToRemove)
            Log.d("MapActivity1", "Маркер удалён для отчёта: ${report.id}")
        } else {
            Log.e("MapActivity1", "Маркер не найден для отчёта: ${report.id}")
        }
    }

    private fun openFiltersDialog() {
        val dialog = FiltersDialog(this, ReportFilters()) { newFilters ->
            applyFilters(newFilters)
        }
        dialog.show()
    }

    private fun applyFilters(filters: ReportFilters) {
        mMap.clear()
        markerReportMap.clear()

        val filteredReports = ReportUtils.filterReports(ReportUtils.loadReports(this), filters)
        for (report in filteredReports) {
            if (report.latitude != null && report.longitude != null) {
                val position = LatLng(report.latitude, report.longitude)
                val iconResource = getIconForActivityType(report.activityType)
                val icon = getIconForActivityType(report.activityType)
                val marker = mMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(report.workType)
                        .icon(icon)
                )
                if (marker != null) {
                    markerReportMap[marker] = report
                }
            }
        }
    }

    private fun exportReports() {
        Toast.makeText(this, "Функция экспорта пока не реализована.", Toast.LENGTH_SHORT).show()
    }
}
