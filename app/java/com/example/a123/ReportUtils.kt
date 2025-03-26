// ReportUtils.kt
package com.example.a123

import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.lang.Exception
import android.content.Context

object ReportUtils {
    private const val TAG = "ReportUtils"

    fun loadReports(context: Context): MutableList<Report> {
        val reports = mutableListOf<Report>()
        val appDir = File(context.getExternalFilesDir(null), "ReportsApp")
        if (appDir.exists()) {
            val files = appDir.listFiles { file -> file.extension == "xml" }
            if (files != null) {
                for (file in files) {
                    val report = parseXmlReport(file.path)
                    report.xmlFilePath = file.path
                    reports.add(report)
                }
            }
        }
        return reports
    }

    fun getRegions(): List<String> {
        return listOf(
            "Астана",
            "Алматы",
            "Шымкент",
            "Абайская область",
            "Актюбинская область",
            "Жетысуская область",
            "Карагандинская область",
            "Кызылординская область",
            "Туркестанская область",
            "Улытауская область",
            "Алматинская область",
            "Костанайская область",
            "Акмолинская область",
            "Атырауская область",
            "Восточно-Казахстанская область",
            "Жамбылская область",
            "Западно-Казахстанская область",
            "Павлодарская область",
            "Северо-Казахстанская область"
        )
    }

    fun getDistrictsForRegion(region: String): List<String> {
        val districtsMap = mapOf(
            "Астана" to listOf(),
            "Алматы" to listOf(),
            "Шымкент" to listOf(),
            "Абайская область" to listOf("Семей", "Курчатов","Абайский", "Аксуатский", "Аягозский", "Бескарагайский", "Бородулихинский", "Жарминский", "Кокпектинский", "Урджарский", "Маканчинский", "Жанасемейский"),
            "Актюбинская область" to listOf("Актобе", "Алгинский", "Айтекебийский", "Байганинский", "Иргизкий", "Карагалинский", "Мартукский", "Мугалжарский", "Темирский", "Уилский", "Хобдинский", "Хромтауский", "Шалкарский"),
            "Жетысуская область" to listOf("Талдыкорган", "Текели", "Аксуский", "Алакольский", "Ескельдинский", "Каратальский", "Кербулакский", "Коксуский", "Панфиловский", "Саркандский"),
            "Карагандинская область" to listOf("Караганда", "Балхаш", "Приозёрск", "Сарань", "Темиртау", "Шахтинск", "Абайский", "Актогайский", "Бухау-Жырауский", "Каркаралинский", "Нуринский", "Осакаровский", "Шетский"),
            "Кызылординская область" to listOf("Кызылорда", "Байконур", "Аральский", "Жалагашский", "Жанакорганский", "Казалинский", "Кармакшинский", "Сырдарьинский", "Шиелийский"),
            "Туркестанская область" to listOf("Туркестан", "Кентау", "Арыс", "Байдибекский", "Казыгурский", "Мактааральский", "Ордабасинский", "Отырарский", "Сайрамский", "Сарыагашский", "Сауранский", "Сузакский", "Толебийский", "Тюлькубаксский", "Шардаринский", "Жетысайский", "Келесский"),
            "Улытауская область" to listOf("Жесказган", "Сатпаев", "Каражал", "Улытауский", "Жанааркинский"),
            "Алматинская область" to listOf("Конаев", "Алатау", "Балхашский", "Енбекшиказахский", "Жамбылский", "Илийский", "Карасайский", "Кегенский", "Райымбекский", "Талгарский", "Уйгурский"),
            "Костанайская область" to listOf("Костанай", "Рудный", "Лисаковск", "Аркалык", "Алтынсаринский", "Амангельдинский", "Аулиекольский", "Денисовский", "Джангельдинский", "Житикаринский", "Камыстинский", "Карабалыкский", "Карасуский", "Костанайский", "Мендыкаринский", "Наурзумский", "Сарыкольский", "Беимбета Майлина", "Узункольский", "Фёдоровский"),
            "Акмолинская область" to listOf("Косшы", "Степногорск", "Кокшетау", "Аккольский", "Аршалыкский", "Астраханский", "Атбасарский", "Буландынский", "Бурабайский", "Егиндыкольский", "Биржан-сал", "Ерейментауский", "Есильский", "Жаксынский", "Жаркаинский", "Зерендинский", "Коргалжинский", "Сандыктауский", "Целиноградский", "Шортандинский"),
            "Атырауская область" to listOf("Атырау", "Жылыойский", "Индерский", "Исатайский", "Кзылкогинский", "Курмангазинский", "Макатайский", "Махамбетский"),
            "Восточно-Казахстанская область" to listOf("Усть-Каменогорск", "Ридер", "Алтайский", "Глубоковский", "Зайсанский", "Катон-Карагайский", "Курчумский", "Маркакольский", "Самарский", "Тарбагайский", "Уланский", "Улькен Нарынский", "Шемонаихинский"),
            "Жамбылская область" to listOf("Тараз", "Байзакский", "Жамбылский", "Жуалынский", "Кордайский", "Меркенский", "Мойынкумский", "Т. Рыскулова", "Сарысуский", "Таласский", "Шуский"),
            "Западно-Казахстанская область" to listOf("Уральск", "Акжаикский", "Бокейординский", "Бурлинский", "Жангалинский", "Жанибекский", "Байтерекский", "Казталовский", "Каратойбинский", "Сырымский", "Таскалинский", "Теректинский", "Чингирлауский"),
            "Павлодарская область" to listOf("Павлодар", "Аксу", "Экибастуз", "Актогайский", "Баянаульский", "Железинский", "Иртышский", "Теренкольский", "Аккулинский", "Майский", "Павлодарский", "Успенский", "Щербактинский"),
            "Северо-Казахстанская область" to listOf("Петропавловск", "Айыртауский", "Акжарский", "Аккайынский", "Есильский", "Жамбылский", "Магжана Жумабаева", "Кызылжарский", "Малютский", "Габита Мусрепова", "Тайыншинский", "Тимирязевский", "Уалихановский", "Шал Акына"),
        )
        return districtsMap[region] ?: listOf()
    }

    fun filterReports(reports: List<Report>, filters: ReportFilters): List<Report> {
        return reports.filter { report ->
            (filters.date.isEmpty() || report.date == filters.date) &&
                    (filters.activityType.isEmpty() || report.activityType == filters.activityType) &&
                    (filters.workType.isEmpty() || report.workType == filters.workType) &&
                    (filters.workPlace.isEmpty() || report.workPlace == filters.workPlace) &&
                    (filters.region.isEmpty() || report.region == filters.region) &&
                    (filters.district.isEmpty() || report.district == filters.district)
        }
    }

    fun parseXmlReport(filePath: String): Report {
        val report = Report()
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(filePath)
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(fis, "UTF-8")

            var eventType = parser.eventType
            var currentTag: String? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        when (currentTag) {
                            "Report" -> {
                                report.dynamicFieldsData = mutableMapOf()
                                report.photos = mutableListOf()
                            }
                            "Photo" -> {
                                parser.next()
                                parser.text?.let { report.photos.add(it) }
                            }
                            "Field" -> {
                                val name = parser.getAttributeValue(null, "name") ?: "Unknown"
                                parser.next()
                                val value = parser.text ?: ""
                                report.dynamicFieldsData[name] = value
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        val text = parser.text ?: ""
                        when (currentTag) {
                            "ID" -> {
                                report.id = text.toLongOrNull() ?: 0
                            }
                            "ActivityType" -> {
                                report.activityType = text
                            }
                            "WorkType" -> {
                                report.workType = text
                            }
                            "WorkPlace" -> {
                                report.workPlace = text
                            }
                            "Culture" -> {
                                report.culture = text
                            }
                            "DevelopmentStage" -> {
                                report.developmentStage = text
                            }
                            "Region" -> {
                                report.region = text
                            }
                            "District" -> {
                                report.district = text
                            }
                            "Executor" -> {
                                report.executor = text
                            }
                            "Area" -> {
                                report.area = text.toDoubleOrNull() ?: 0.0
                            }
                            "FarmName" -> {
                                report.farmName = text
                            }
                            "Latitude" -> {
                                report.latitude = text.toDoubleOrNull() ?: 0.0
                            }
                            "Longitude" -> {
                                report.longitude = text.toDoubleOrNull() ?: 0.0
                            }
                            "Description" -> {
                                report.description = text
                            }
                            "Results" -> {
                                report.results = text
                            }
                            "Date" -> {
                                report.date = text
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        currentTag = null
                    }
                }
                eventType = parser.next()
            }
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "Файл не найден: $filePath", e)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при парсинге файла $filePath", e)
        } finally {
            fis?.close()
        }
        return report
    }

    fun saveReport(report: Report, file: File) {
        try {
            val serializer = Xml.newSerializer()
            val fos = FileOutputStream(file)
            serializer.setOutput(fos, "UTF-8")
            serializer.startDocument("UTF-8", true)
            serializer.startTag("", "Report")

            // Сохранение всех полей отчета
            serializer.startTag("", "ID")
            serializer.text(report.id.toString())
            serializer.endTag("", "ID")

            serializer.startTag("", "ActivityType")
            serializer.text(report.activityType)
            serializer.endTag("", "ActivityType")

            serializer.startTag("", "WorkType")
            serializer.text(report.workType)
            serializer.endTag("", "WorkType")

            serializer.startTag("", "WorkPlace")
            serializer.text(report.workPlace)
            serializer.endTag("", "WorkPlace")

            serializer.startTag("", "Culture")
            serializer.text(report.culture)
            serializer.endTag("", "Culture")

            serializer.startTag("", "DevelopmentStage")
            serializer.text(report.developmentStage)
            serializer.endTag("", "DevelopmentStage")

            serializer.startTag("", "Region")
            serializer.text(report.region)
            serializer.endTag("", "Region")

            serializer.startTag("", "District")
            serializer.text(report.district)
            serializer.endTag("", "District")

            serializer.startTag("", "Executor")
            serializer.text(report.executor)
            serializer.endTag("", "Executor")

            serializer.startTag("", "Area")
            serializer.text(report.area.toString())
            serializer.endTag("", "Area")

            serializer.startTag("", "FarmName")
            serializer.text(report.farmName)
            serializer.endTag("", "FarmName")

            serializer.startTag("", "Latitude")
            serializer.text(report.latitude.toString())
            serializer.endTag("", "Latitude")

            serializer.startTag("", "Longitude")
            serializer.text(report.longitude.toString())
            serializer.endTag("", "Longitude")

            serializer.startTag("", "Description")
            serializer.text(report.description)
            serializer.endTag("", "Description")

            serializer.startTag("", "Results")
            serializer.text(report.results)
            serializer.endTag("", "Results")

            serializer.startTag("", "Date")
            serializer.text(report.date)
            serializer.endTag("", "Date")

            // Сохранение динамических полей
            serializer.startTag("", "DynamicFieldsData")
            for ((key, value) in report.dynamicFieldsData) {
                serializer.startTag("", "Field")
                serializer.attribute("", "name", key)
                serializer.text(value)
                serializer.endTag("", "Field")
            }
            serializer.endTag("", "DynamicFieldsData")

            serializer.endTag("", "Report")
            serializer.endDocument()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
