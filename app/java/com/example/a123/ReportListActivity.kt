package com.example.a123

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.IOException

/**
 * Активность для отображения списка отчётов.
 */
class ReportListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReportAdapter
    private var reportList: MutableList<Report> = mutableListOf()
    private var filteredReportList: MutableList<Report> = mutableListOf()
    private var currentFilters: ReportFilters = ReportFilters()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_list)

        // Получаем текущие фильтры из Intent
        currentFilters = intent.getSerializableExtra("filters") as? ReportFilters ?: ReportFilters()

        // Инициализация RecyclerView
        recyclerView = findViewById(R.id.recyclerViewReports1)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Инициализация адаптера
        adapter = ReportAdapter(
            reports = filteredReportList,
            onItemClick = { report -> openReportForViewing(report) },
            onItemLongClick = { report -> showDeleteConfirmationDialog(report) }
        )
        recyclerView.adapter = adapter

        // Загрузка отчётов и применение фильтров
        loadReports()
        applyFilters()

        // Обработка кнопок
        findViewById<Button>(R.id.buttonCreateReport1).setOnClickListener {
            val intent = Intent(this, CreateReportActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.buttonShowOnMap1).setOnClickListener {
            val intent = Intent(this, MapActivity1::class.java)
            intent.putExtra("filters", currentFilters)
            startActivity(intent)
        }

        findViewById<Button>(R.id.buttonFilters1).setOnClickListener {
            openFiltersDialog()
        }

        findViewById<Button>(R.id.buttonExportReports1).setOnClickListener {
            exportReports()
        }
    }

    override fun onResume() {
        super.onResume()
        loadReports()
        applyFilters()
        adapter.notifyDataSetChanged()
    }

    /**
     * Загружает отчёты из хранилища.
     */
    private fun loadReports() {
        val reports = ReportUtils.loadReports(this)
        reportList = reports.map { report ->
            val photos = extractPhotoPathsFromXml(File(report.xmlFilePath))
            report.copy(photos = photos.toMutableList())
        }.toMutableList()
    }

    /**
     * Применяет текущие фильтры к списку отчётов.
     */
    private fun applyFilters() {
        filteredReportList.clear()
        filteredReportList.addAll(ReportUtils.filterReports(reportList, currentFilters))
        adapter.updateData(filteredReportList)
    }

    /**
     * Открывает отчёт для просмотра.
     */
    private fun openReportForViewing(report: Report) {
        val intent = Intent(this, ReportViewActivity::class.java)
        intent.putExtra("xmlFilePath", report.xmlFilePath)
        startActivity(intent)
    }

    /**
     * Открывает диалоговое окно с подтверждением удаления отчёта.
     */
    private fun showDeleteConfirmationDialog(report: Report) {
        AlertDialog.Builder(this)
            .setTitle("Удалить отчёт")
            .setMessage("Вы уверены, что хотите удалить этот отчёт?")
            .setPositiveButton("Да") { _, _ -> deleteReport(report) }
            .setNegativeButton("Нет", null)
            .show()
    }

    /**
     * Удаляет отчёт из хранилища и обновляет список.
     */
    private fun deleteReport(report: Report) {
        val file = File(report.xmlFilePath)
        if (file.exists()) {
            if (file.delete()) {
                reportList.remove(report)
                applyFilters()
                Toast.makeText(this, "Отчёт удалён.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Не удалось удалить отчёт.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Файл отчёта не найден.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Открывает диалоговое окно для применения фильтров.
     */
    private fun openFiltersDialog() {
        val dialog = FiltersDialog(this, currentFilters) { newFilters ->
            currentFilters = newFilters
            applyFilters()
        }
        dialog.show()
    }

    /**
     * Экспортирует отчёты на сервер.
     */
    private fun exportReports() {
        val client = OkHttpClient()
        var successCount = 0
        var failureCount = 0

        for (report in filteredReportList) {
            val xmlFile = File(report.xmlFilePath)
            if (!xmlFile.exists()) {
                failureCount++
                continue
            }

            val photoPaths = extractPhotoPathsFromXml(xmlFile)
            val requestBodyBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("xml_file", xmlFile.name, xmlFile.asRequestBody("application/xml".toMediaTypeOrNull()))

            for (photoPath in photoPaths) {
                val photoFile = File(photoPath)
                if (photoFile.exists()) {
                    requestBodyBuilder.addFormDataPart(
                        "photos",
                        photoFile.name,
                        photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    )
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Файл $photoPath не найден.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            val requestBody = requestBodyBuilder.build()
            val request = Request.Builder()
                .url("http://172.30.104.148:8080/upload")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    failureCount++
                    runOnUiThread {
                        Toast.makeText(this@ReportListActivity, "Ошибка отправки отчёта: ${xmlFile.name}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        successCount++
                        runOnUiThread {
                            Toast.makeText(this@ReportListActivity, "Отчёт ${xmlFile.name} успешно отправлен!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        failureCount++
                        runOnUiThread {
                            Toast.makeText(this@ReportListActivity, "Ошибка сервера при отправке отчёта: ${xmlFile.name}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
    }

    /**
     * Извлекает пути к фотографиям из XML-файла.
     */
    private fun extractPhotoPathsFromXml(xmlFile: File): List<String> {
        val photoPaths = mutableListOf<String>()
        try {
            val xmlFactory = XmlPullParserFactory.newInstance()
            val xmlParser = xmlFactory.newPullParser()
            xmlParser.setInput(xmlFile.inputStream(), "UTF-8")
            var eventType = xmlParser.eventType

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && xmlParser.name == "Photo") {
                    val path = xmlParser.nextText()
                    photoPaths.add(path)
                }
                eventType = xmlParser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return photoPaths
    }
}
