// Report.kt
package com.example.a123

import java.io.Serializable

data class Report(
    var id: Long = 0,
    var activityType: String = "",
    var workType: String = "",
    var workPlace: String = "",
    var culture: String = "",
    var developmentStage: String = "",
    var region: String = "",
    var district: String = "",
    var executor: String = "",
    var area: Double = 0.0,
    var farmName: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var description: String = "",
    var photos: MutableList<String> = mutableListOf(),
    var results: String = "",
    var date: String = "",
    var dynamicFieldsData: MutableMap<String, String> = mutableMapOf(),
    var xmlFilePath: String = "" // Добавлено поле
)

