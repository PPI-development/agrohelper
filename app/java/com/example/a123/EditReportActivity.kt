package com.example.a123

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.File
import java.io.FileOutputStream
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.EditText
import android.widget.LinearLayout

class EditReportActivity : AppCompatActivity() {

    private lateinit var editActivityTypeSpinner: Spinner
    private lateinit var editWorkTypeSpinner: Spinner
    private lateinit var editWorkPlaceSpinner: Spinner
    private lateinit var editCultureSpinner: Spinner
    private lateinit var editDevelopmentStageSpinner: Spinner
    private lateinit var editRegionSpinner: Spinner
    private lateinit var editDistrictSpinner: Spinner
    private lateinit var editExecutorSpinner: Spinner
    private lateinit var editAreaEditText: EditText
    private lateinit var editFarmNameEditText: EditText
    private lateinit var editLatitudeEditText: EditText
    private lateinit var editLongitudeEditText: EditText
    private lateinit var editDescriptionEditText: EditText
    private lateinit var dynamicFieldsLayout: LinearLayout
    private lateinit var photosLayout: LinearLayout
    private lateinit var buttonAddPhoto: Button
    private lateinit var buttonSaveChanges: Button

    private var report: Report? = null
    private var xmlFilePath: String = ""
    private val REQUEST_IMAGE_CAPTURE = 1001
    private val REQUEST_IMAGE_PICK = 1003
    private val REQUEST_PERMISSIONS_CODE = 1004

    private val newPhotos = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_report)

        photosLayout = findViewById(R.id.photosLayout)

        xmlFilePath = intent.getStringExtra("xmlFilePath") ?: ""
        if (xmlFilePath.isNotEmpty()) {
            report = ReportUtils.parseXmlReport(xmlFilePath)
            report?.xmlFilePath = xmlFilePath
            setupSpinners()
            report?.let { populateFields(it) }
        } else {
            Toast.makeText(this, "Путь к файлу отчёта не передан.", Toast.LENGTH_SHORT).show()
            finish()
        }

        buttonSaveChanges = findViewById(R.id.buttonSaveChanges)
        buttonSaveChanges.setOnClickListener {
            report?.let { saveReportToXml(it) }
        }

        buttonAddPhoto = findViewById(R.id.buttonAddPhoto)
        buttonAddPhoto.setOnClickListener {
            showImageSourceDialog()
        }
    }

    private fun setupSpinners() {
        editActivityTypeSpinner = findViewById(R.id.editActivityTypeSpinner)
        editWorkTypeSpinner = findViewById(R.id.editWorkTypeSpinner)
        editWorkPlaceSpinner = findViewById(R.id.editWorkPlaceSpinner)
        editCultureSpinner = findViewById(R.id.editCultureSpinner)
        editDevelopmentStageSpinner = findViewById(R.id.editDevelopmentStageSpinner)
        editRegionSpinner = findViewById(R.id.editRegionSpinner)
        editDistrictSpinner = findViewById(R.id.editDistrictSpinner)
        editExecutorSpinner = findViewById(R.id.editExecutorSpinner)

        val activityTypes = listOf("Энтомолог", "Фитопатолог", "Герболог")
        val workTypes = listOf("Первичный мониторинг", "Проведение осмотра Вредных Организмов")
        val workPlaces = listOf("Поле", "Сад", "Теплица", "Лесопосадка")
        val culturesField = listOf("Пшеница", "Кукуруза", "Ячмень", "Соя", "Картофель", "Рапс", "Томат", "Подсолнух", "Рис", "Хлопок")
        val culturesGarden = listOf("Яблоня", "Абрикос", "Груша", "Виноград")
        val culturesGreenhouse = listOf("Томат", "Клубника", "Огурец")
        val developmentStages = listOf("Зародышевый", "Вегетативный", "Цветущий", "Плодоносящий", "Закатной")
        val regions = ReportUtils.getRegions()
        val executors = listOf("Дуйсембеков Б.А.", "Ниязбеков Ж.Б.", "Успанов А.М.", "Копжасаров Б.К.", "Есимов У.О.", "Болтаев М.Д.", "Ермекбаев Б.У.", "Болтаева Л.А.", "Тайшиков М.А.", "Никоноров А.П.", "Фазылбеков Р.Р.", "Рысбекова А.М.", "Успанов У.Т.")

        setupSpinner(editActivityTypeSpinner, activityTypes, report?.activityType ?: "")
        setupSpinner(editWorkTypeSpinner, workTypes, report?.workType ?: "")
        setupSpinner(editWorkPlaceSpinner, workPlaces, report?.workPlace ?: "")
        setupSpinner(editDevelopmentStageSpinner, developmentStages, report?.developmentStage ?: "")
        setupSpinner(editRegionSpinner, regions, report?.region ?: "")
        setupSpinner(editExecutorSpinner, executors, report?.executor ?: "")

        updateCultureSpinner(report?.workPlace ?: "")
        updateDistrictSpinner(report?.region ?: "")

        editWorkPlaceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateCultureSpinner(editWorkPlaceSpinner.selectedItem.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        editRegionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateDistrictSpinner(editRegionSpinner.selectedItem.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun saveReportToXml(report: Report) {
        val file = File(report.xmlFilePath)
        if (file.exists()) {
            report.activityType = editActivityTypeSpinner.selectedItem?.toString() ?: ""
            report.workType = editWorkTypeSpinner.selectedItem?.toString() ?: ""
            report.workPlace = editWorkPlaceSpinner.selectedItem?.toString() ?: ""
            report.culture = editCultureSpinner.selectedItem?.toString() ?: ""
            report.developmentStage = editDevelopmentStageSpinner.selectedItem?.toString() ?: ""
            report.region = editRegionSpinner.selectedItem?.toString() ?: ""
            report.district = editDistrictSpinner.selectedItem?.toString() ?: ""
            report.executor = editExecutorSpinner.selectedItem?.toString() ?: ""
            report.area = editAreaEditText.text.toString().toDoubleOrNull() ?: 0.0
            report.farmName = editFarmNameEditText.text.toString()
            report.latitude = editLatitudeEditText.text.toString().toDoubleOrNull() ?: 0.0
            report.longitude = editLongitudeEditText.text.toString().toDoubleOrNull() ?: 0.0
            report.description = editDescriptionEditText.text.toString()

            val updatedPhotos = (report.photos + newPhotos).toMutableList()
            report.photos = updatedPhotos

            ReportUtils.saveReport(report, file)
            Toast.makeText(this, "Отчёт сохранён", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Файл отчёта не найден", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf<CharSequence>("Сделать фото", "Выбрать из галереи", "Отмена")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Добавить фото")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Сделать фото" -> {
                    openCamera()
                }
                options[item] == "Выбрать из галереи" -> {
                    openGallery()
                }
                options[item] == "Отмена" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    imageBitmap?.let {
                        saveImageToStorage(it)
                    }
                }
                REQUEST_IMAGE_PICK -> {
                    val imageUri = data?.data
                    imageUri?.let {
                        saveImageUriToStorage(it)
                    }
                }
            }
        }
    }

    private fun saveImageToStorage(bitmap: Bitmap) {
        val appDir = File(getExternalFilesDir(null), "ReportsApp")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        val filename = "photo_${System.currentTimeMillis()}.jpg"
        val file = File(appDir, filename)
        FileOutputStream(file).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        }
        newPhotos.add(file.absolutePath)
        displayPhotos(report?.photos.orEmpty() + newPhotos)
    }

    private fun saveImageUriToStorage(imageUri: Uri) {
        val appDir = File(getExternalFilesDir(null), "ReportsApp")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        val filename = "photo_${System.currentTimeMillis()}.jpg"
        val file = File(appDir, filename)

        try {
            contentResolver.openInputStream(imageUri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            newPhotos.add(file.absolutePath)
            displayPhotos(report?.photos.orEmpty() + newPhotos)
            Toast.makeText(this, "Фото добавлено из галереи", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка при добавлении фото", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayPhotos(photos: List<String>) {
        photosLayout.removeAllViews()
        for (photoPath in photos) {
            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 16
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            Glide.with(this)
                .load(photoPath)
                .apply(RequestOptions().placeholder(R.drawable.placeholder_image).centerCrop())
                .into(imageView)

            photosLayout.addView(imageView)
        }
    }

    private fun setupSpinner(spinner: Spinner, items: List<String>, selectedValue: String) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(items.indexOf(selectedValue))
    }

    private fun updateCultureSpinner(workPlace: String) {
        val cultures = when (workPlace) {
            "Поле" -> listOf("Пшеница", "Кукуруза", "Ячмень", "Соя", "Картофель", "Рапс", "Томат", "Подсолнух", "Рис", "Хлопок")
            "Сад" -> listOf("Яблоня", "Абрикос", "Груша", "Виноград")
            "Теплица" -> listOf("Томат", "Клубника", "Огурец")
            else -> listOf()
        }
        setupSpinner(editCultureSpinner, cultures, report?.culture ?: "")
    }

    private fun updateDistrictSpinner(region: String) {
        val districts = ReportUtils.getDistrictsForRegion(region)
        setupSpinner(editDistrictSpinner, districts, report?.district ?: "")
    }

    private fun populateFields(report: Report) {
        editAreaEditText = findViewById(R.id.editAreaEditText)
        editFarmNameEditText = findViewById(R.id.editFarmNameEditText)
        editLatitudeEditText = findViewById(R.id.editLatitudeEditText)
        editLongitudeEditText = findViewById(R.id.editLongitudeEditText)
        editDescriptionEditText = findViewById(R.id.editDescriptionEditText)
        dynamicFieldsLayout = findViewById(R.id.dynamicFieldsLayout)
        photosLayout = findViewById(R.id.photosLayout)

        editAreaEditText.setText(report.area.toString())
        editFarmNameEditText.setText(report.farmName)
        editLatitudeEditText.setText(report.latitude.toString())
        editLongitudeEditText.setText(report.longitude.toString())
        editDescriptionEditText.setText(report.description)

        // Отображаем динамические поля
        populateDynamicFields(report)
        // Отображаем фотографии
        displayPhotos(report.photos)
    }

    private fun populateDynamicFields(report: Report) {
        dynamicFieldsLayout.removeAllViews()
        for ((key, value) in report.dynamicFieldsData) {
            val textView = TextView(this).apply {
                text = key
                setTextColor(Color.BLACK)
            }
            val editText = EditText(this).apply {
                setText(value)
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            dynamicFieldsLayout.addView(textView)
            dynamicFieldsLayout.addView(editText)
        }
    }
}
