package com.example.a123
import com.google.android.gms.maps.model.LatLng

data class FieldData(
    val id: String,
    var name: String,
    var points: List<LatLng>, // Список точек полигона
    var crop: String,
    var soilType: String,
    var area: Double, // Площадь поля в гектарах
    var tasks: MutableList<TaskData> = mutableListOf() // Используем изменяемый список для задач
)
