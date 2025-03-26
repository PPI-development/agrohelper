package com.example.a123

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.util.*
import android.graphics.Color

class Pole : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var xmlHelper: FieldXmlHelper
    private val polygonPoints = mutableListOf<LatLng>()
    private var isEditingMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pole)

        xmlHelper = FieldXmlHelper(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment5) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val addFieldButton: Button = findViewById(R.id.buttonAddField5)
        addFieldButton.setOnClickListener {
            if (!isEditingMode) {
                isEditingMode = true
                addFieldButton.text = "Завершить"
                polygonPoints.clear()
            } else {
                if (polygonPoints.size > 2) {
                    showFieldNameDialog { fieldName ->
                        // Рассчитываем площадь
                        val area = String.format("%.2f", calculatePolygonArea(polygonPoints) / 10000).replace(",", ".").toDouble() // Преобразуем площадь из кв. метров в гектары
                        // Преобразуем площадь из кв. метров в гектары

                        // Создаем новый объект FieldData, передавая рассчитанную площадь
                        val newField = FieldData(
                            UUID.randomUUID().toString(),
                            fieldName,
                            polygonPoints,
                            "Пшеница", // Предварительно устанавливаем культуру
                            "Чернозем", // Предварительно устанавливаем тип почвы
                            area // Передаем рассчитанную площадь
                        )

                        xmlHelper.saveField(newField)  // Сохраняем поле в XML
                        drawPolygon(newField)
                        returnFieldNameToMainActivity(fieldName)  // Возвращаем имя поля в основную активность
                    }
                }
                isEditingMode = false
                addFieldButton.text = "Добавить поле"
            }
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(43.238949, 76.889709), 12f))

        // Загрузка всех полей из XML
        val fields = xmlHelper.loadFields()
        fields.forEach { drawPolygon(it) }

        map.setOnMapClickListener { latLng ->
            if (isEditingMode) {
                addPointToPolygon(latLng)
            }
        }

        map.setOnPolygonClickListener { polygon ->
            val fieldId = polygon.tag as? String
            fieldId?.let { showPolygonInfoDialog(it) }  // Отображаем информацию о полигоне
        }
    }

    private fun addPointToPolygon(latLng: LatLng) {
        polygonPoints.add(latLng)

        map.addCircle(
            CircleOptions()
                .center(latLng)
                .radius(5.0)
                .strokeColor(Color.RED)
                .fillColor(Color.RED)
                .strokeWidth(2f)
        )
    }

    private val mapPolygons = mutableListOf<Polygon>()

    private fun drawPolygon(fieldData: FieldData) {
        val polygonOptions = PolygonOptions()
            .addAll(fieldData.points)
            .strokeColor(Color.GREEN)
            .fillColor(0x7F00FF00) // Полупрозрачный зелёный
            .strokeWidth(5f)
            .clickable(true)  // Делаем полигон кликабельным

        val polygon = map.addPolygon(polygonOptions)
        polygon.tag = fieldData.id  // Назначаем полигону уникальный идентификатор
        mapPolygons.add(polygon)  // Добавляем в список
    }

    private fun showFieldNameDialog(onFieldNameEntered: (String) -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Введите название поля")
        val input = EditText(this)
        input.hint = "Название поля"
        builder.setView(input)
        builder.setPositiveButton("OK") { _, _ -> onFieldNameEntered(input.text.toString()) }
        builder.setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    // Функция для возврата имени поля в основную активность
    private fun returnFieldNameToMainActivity(fieldName: String) {
        val intent = Intent().apply {
            putExtra("fieldName", fieldName)
        }
        setResult(RESULT_OK, intent)
         // Закрытие активности Pole и возвращение данных в основную активность
    }

    private fun showPolygonInfoDialog(fieldId: String) {
        // Ищем полигон по ID
        val fieldData = xmlHelper.loadFields().find { it.id == fieldId }

        fieldData?.let {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Информация о поле")

            // Вычисляем площадь полигона
            val area = String.format("%.2f", calculatePolygonArea(it.points))

            // Добавляем кнопку "Подробнее"
            builder.setPositiveButton("Подробнее") { _, _ ->
                // Переход в новую активность с подробной информацией о поле
                openFieldDetailActivity(it.id)
            }

            // Добавляем кнопку "Удалить"
            builder.setNegativeButton("Удалить") { _, _ ->
                // Удаляем поле
                deleteField(it.id)
            }

            // Добавляем нейтральную кнопку "OK"
            builder.setNeutralButton("OK") { dialog, _ -> dialog.dismiss() }

            // Отображаем информацию о поле
            builder.setMessage("Название: ${it.name}\nПлощадь: $area кв.м")
            builder.show()
        }
    }


    private fun deleteField(fieldId: String) {
        // Ищем полигон с данным идентификатором по тегу
        val polygons = mapPolygons.filter { it.tag == fieldId }

        // Удаляем найденные полигоны
        polygons.forEach {
            it.remove()
            mapPolygons.remove(it)  // Удаляем из списка
        }

        // Удаляем данные о поле из XML
        xmlHelper.deleteField(fieldId)
    }







    // Функция для открытия активности с подробностями о поле
    private fun openFieldDetailActivity(fieldId: String) {
        val intent = Intent(this, FieldDetailActivity::class.java)
        intent.putExtra("fieldId", fieldId)
        startActivity(intent)
    }

    private fun saveFieldWithArea(fieldName: String) {
        if (polygonPoints.size > 2) {
            val area = calculatePolygonArea(polygonPoints) / 10000 // Преобразуем из кв. метров в гектары
            val newField = FieldData(
                UUID.randomUUID().toString(),
                fieldName,
                polygonPoints,
                "Пшеница", // Предварительно устанавливаем культуру
                "Чернозем", // Предварительно устанавливаем тип почвы
                area // Сохраняем площадь в гектарах
            )
            xmlHelper.saveField(newField)
            drawPolygon(newField)
        }
    }


    private fun calculatePolygonArea(points: List<LatLng>): Double {
        return com.google.maps.android.SphericalUtil.computeArea(points)  // Возвращаем площадь в квадратных метрах
    }
}
