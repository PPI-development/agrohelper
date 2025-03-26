package com.example.a123

import android.content.Context
import android.util.Xml
import com.google.android.gms.maps.model.LatLng
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class FieldXmlHelper(private val context: Context) {

    private val fileName = "fields_data.xml"

    // Сохранение поля в XML
    fun saveField(fieldData: FieldData) {
        // Сначала загружаем все существующие поля
        val existingFields = loadFields().toMutableList()

        // Добавляем новое поле в список
        existingFields.add(fieldData)

        // Сохраняем все поля в файл
        saveAllFields(existingFields)
    }

    fun deleteField(fieldId: String) {
        // Загружаем все поля
        val fields = loadFields().toMutableList()

        // Находим поле по ID и удаляем его из списка
        val fieldToRemove = fields.find { it.id == fieldId }
        fieldToRemove?.let {
            fields.remove(it)
        }

        // Сохраняем оставшиеся поля обратно в XML
        saveAllFields(fields)
    }

    // Сохранение всех полей (перезапись файла)
    // Сохранение всех полей (перезапись файла)
    private fun saveAllFields(fields: List<FieldData>) {
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file, false) // false означает перезапись файла

        val serializer: XmlSerializer = Xml.newSerializer()
        serializer.setOutput(outputStream, "UTF-8")
        serializer.startDocument("UTF-8", true)

        // Записываем все поля, включая новые
        serializer.startTag(null, "fields")  // Основной тег для всех полей

        fields.forEach { field ->
            serializer.startTag(null, "field")
            serializer.attribute(null, "id", field.id)

            serializer.startTag(null, "name")
            serializer.text(field.name)
            serializer.endTag(null, "name")

            serializer.startTag(null, "points")
            field.points.forEach { point ->
                serializer.startTag(null, "point")
                serializer.attribute(null, "lat", point.latitude.toString())
                serializer.attribute(null, "lng", point.longitude.toString())
                serializer.endTag(null, "point")
            }
            serializer.endTag(null, "points")

            serializer.startTag(null, "tasks")
            field.tasks.forEach { task ->
                serializer.startTag(null, "task")
                serializer.attribute(null, "timeInMillis", task.timeInMillis.toString()) // Сохраняем время задачи
                serializer.text(task.description) // Сохраняем описание задачи
                serializer.endTag(null, "task")
            }
            serializer.endTag(null, "tasks")

            serializer.startTag(null, "crop")
            serializer.text(field.crop)
            serializer.endTag(null, "crop")

            serializer.startTag(null, "soilType")
            serializer.text(field.soilType)
            serializer.endTag(null, "soilType")

            serializer.startTag(null, "area")
            serializer.text(String.format("%.2f", field.area))  // Ограничиваем площадь до 2 знаков после запятой
            serializer.endTag(null, "area")

            serializer.endTag(null, "field")
        }

        serializer.endTag(null, "fields")  // Закрываем основной тег
        serializer.endDocument()

        outputStream.close()
    }



    // Загрузка всех полей из XML
    fun loadFields(): List<FieldData> {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return emptyList() // Если файл не существует, возвращаем пустой список

        val inputStream = FileInputStream(file)
        val parserFactory = XmlPullParserFactory.newInstance()
        val parser = parserFactory.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        val fields = mutableListOf<FieldData>()
        var eventType = parser.eventType
        var currentField: FieldData? = null
        var points = mutableListOf<LatLng>()
        var tasks = mutableListOf<TaskData>() //
        var currentTag = ""
        var area: Double = 0.0

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = tagName
                    if (tagName == "field") {
                        val id = parser.getAttributeValue(null, "id")
                        currentField = FieldData(id, "", emptyList(), "", "", 0.0)
                        points = mutableListOf()
                        tasks = mutableListOf()  // Инициализируем список задач
                    } else if (tagName == "point") {
                        val lat = parser.getAttributeValue(null, "lat").toDouble()
                        val lng = parser.getAttributeValue(null, "lng").toDouble()
                        points.add(LatLng(lat, lng))
                    } else if (tagName == "task") {
                        val timeInMillis = parser.getAttributeValue(null, "timeInMillis").toLong() // Загружаем время
                        val description = parser.nextText() // Загружаем описание задачи
                        tasks.add(TaskData(description, timeInMillis))  // Добавляем задачу в список
                    }
                }
                XmlPullParser.TEXT -> {
                    currentField?.let {
                        when (currentTag) {
                            "name" -> it.name = parser.text
                            "area" -> area = parser.text.replace(",", ".").toDouble()
                            "crop" -> it.crop = parser.text
                            "soilType" -> it.soilType = parser.text
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (tagName == "field" && currentField != null) {
                        currentField.points = points
                        currentField.area = area
                        currentField.tasks = tasks // Присваиваем задачи полю
                        fields.add(currentField)
                    }
                }
            }
            eventType = parser.next()
        }

        inputStream.close()
        return fields
    }


    // Функция для обновления поля
    fun updateField(updatedField: FieldData) {
        val fields = loadFields().toMutableList()
        val index = fields.indexOfFirst { it.id == updatedField.id }
        if (index != -1) {
            fields[index] = updatedField
            saveAllFields(fields) // Перезаписываем файл с обновленными данными
        }
    }
}
