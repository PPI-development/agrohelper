// CreateReportActivity.kt

package com.example.a123

import android.Manifest
import androidx.appcompat.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.util.*
import android.graphics.Color
import android.util.Xml
import org.xmlpull.v1.XmlSerializer
import android.net.Uri


class CreateReportActivity : AppCompatActivity() {

    private lateinit var activityTypeSpinner: Spinner
    private lateinit var workTypeSpinner: Spinner
    private lateinit var workPlaceSpinner: Spinner
    private lateinit var cultureSpinner: Spinner
    private lateinit var developmentStageSpinner: Spinner
    private lateinit var regionSpinner: Spinner
    private lateinit var districtSpinner: Spinner
    private lateinit var workerSpinner: Spinner
    private lateinit var areaEditText: EditText
    private lateinit var farmEditText: EditText
    private lateinit var saveLocationButton: Button
    private lateinit var dynamicFieldsLayout: LinearLayout
    private lateinit var descriptionEditText: EditText
    private lateinit var takePhotoButton: Button
    private lateinit var dateButton: Button
    private lateinit var saveButton: Button
    private lateinit var locationTextView: TextView

    private var selectedLocation: Location? = null
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    private val REQUEST_PERMISSIONS_CODE = 1001
    private val CAMERA_PERMISSION_CODE = 1002
    private val LOCATION_PERMISSION_CODE = 1003
    private val REQUEST_IMAGE_CAPTURE = 1001
    private val REQUEST_IMAGE_PICK = 1003



    private val photosList = mutableListOf<String>()
    private val executorList = listOf("Дуйсембеков Б.А.", "Ниязбеков Ж.Б.", "Успанов А.М.", "Копжасаров Б.К.", "Есимов У.О.", "Болтаев М.Д.", "Ермекбаев Б.У.", "Болтаева Л.А.", "Тайшиков М.А.", "Никоноров А.П.", "Фазылбеков Р.Р.", "Рысбекова А.М.", "Усманов У.Т.")
    private val regions = listOf(
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
    private val districtsMap = mapOf(
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

    private val activityTypes = listOf("Энтомолог", "Фитопатолог", "Герболог")
    private val workTypes = listOf("Первичный мониторинг", "Проведение осмотра Вредных Организмов")
    private val workPlaces = listOf("Поле", "Сад", "Теплица", "Лесопосадка")
    private val culturesField = listOf("Пшеница", "Кукуруза", "Ячмень", "Соя", "Картофель", "Рапс", "Томат", "Подсолнух", "Рис", "Хлопок")
    private val culturesGarden = listOf("Яблоня", "Абрикос", "Груша", "Виноград")
    private val culturesGreenhouse = listOf("Томат", "Клубника", "Огурец")
    private val developmentStages = listOf(
        "Зародышевый",
        "Вегетативный",
        "Цветущий",
        "Плодоносящий",
        "Закатной"
    )


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
        photosList.add(file.absolutePath)
        Toast.makeText(this, "Фото добавлено с камеры", Toast.LENGTH_SHORT).show()
    }

    private fun saveImageUriToStorage(imageUri: Uri) {
        val appDir = File(getExternalFilesDir(null), "ReportsApp")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        val filename = "photo_${System.currentTimeMillis()}.jpg"
        val file = File(appDir, filename)

        try {
            val inputStream = contentResolver.openInputStream(imageUri)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            photosList.add(file.absolutePath)
            Toast.makeText(this, "Фото добавлено из галереи", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка при добавлении фото", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf<CharSequence>("Сделать фото", "Выбрать из галереи", "Отмена")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Добавить фото")
        builder.setItems(options) { dialog, item ->
            when (options[item]) {
                "Сделать фото" -> {
                    openCamera()
                }
                "Выбрать из галереи" -> {
                    openGallery()
                }
                "Отмена" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_report)

        // Инициализация UI компонентов
        activityTypeSpinner = findViewById(R.id.activityTypeSpinner)
        workTypeSpinner = findViewById(R.id.workTypeSpinner)
        workPlaceSpinner = findViewById(R.id.workPlaceSpinner)
        cultureSpinner = findViewById(R.id.cultureSpinner)
        developmentStageSpinner = findViewById(R.id.developmentStageSpinner)
        regionSpinner = findViewById(R.id.regionSpinner)
        districtSpinner = findViewById(R.id.districtSpinner)
        workerSpinner = findViewById(R.id.workerSpinner)
        areaEditText = findViewById(R.id.areaEditText)
        farmEditText = findViewById(R.id.farmEditText)
        saveLocationButton = findViewById(R.id.saveLocationButton)
        dynamicFieldsLayout = findViewById(R.id.dynamicFieldsLayout)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        takePhotoButton = findViewById(R.id.takePhotoButton)
        dateButton = findViewById(R.id.dateButton)
        saveButton = findViewById(R.id.saveButton)
        locationTextView = findViewById(R.id.locationTextView)

        setupSpinners()
        initializeLocationManager()
        setupListeners()
        startLocationUpdates()

        // Устанавливаем текущую дату по умолчанию
        val calendar = Calendar.getInstance()
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateButton.text = dateFormat.format(calendar.time)
    }

    private fun setupSpinners() {
        // Вид деятельности
        val activityAdapter = createBlackTextAdapter(activityTypes)
        activityTypeSpinner.adapter = activityAdapter

        // Вид работы
        val workTypeAdapter = createBlackTextAdapter(workTypes)
        workTypeSpinner.adapter = workTypeAdapter

        // Место проведения работ
        val workPlaceAdapter = createBlackTextAdapter(workPlaces)
        workPlaceSpinner.adapter = workPlaceAdapter

        // Исполнитель
        val workerAdapter = createBlackTextAdapter(executorList)
        workerSpinner.adapter = workerAdapter

        // Области Казахстана
        val regionAdapter = createBlackTextAdapter(regions)
        regionSpinner.adapter = regionAdapter

        // Фаза развития
        val developmentStageAdapter = createBlackTextAdapter(developmentStages)
        developmentStageSpinner.adapter = developmentStageAdapter
    }

    private fun createBlackTextAdapter(data: List<String>): ArrayAdapter<String> {
        return object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(Color.BLACK)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(Color.WHITE)
                return view
            }
        }.apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun initializeLocationManager() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                selectedLocation = location
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10f, locationListener)
        }
    }

    private fun setupListeners() {
        workPlaceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateCultureSpinner(workPlaceSpinner.selectedItem.toString())
            }
        }

        regionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateDistrictSpinner(regionSpinner.selectedItem.toString())
            }
        }

        activityTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateDynamicFields(activityTypeSpinner.selectedItem.toString())
            }
        }

        saveLocationButton.setOnClickListener {
            selectedLocation?.let {
                val coordinates = "Координаты: Latitude: ${it.latitude}, Longitude: ${it.longitude}"
                locationTextView.text = coordinates
                Toast.makeText(this, coordinates, Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(this, "Местоположение не выбрано", Toast.LENGTH_SHORT).show()
            }
        }


        dateButton.setOnClickListener {
            showDatePicker()
        }

        takePhotoButton.setOnClickListener {
            if (checkAndRequestPermissions()) {
                showImageSourceDialog()
            }
        }

        saveButton.setOnClickListener {
            val report = collectReportData()
            saveReport(report)
        }
    }

    private fun updateCultureSpinner(workPlace: String) {
        val cultures = when (workPlace) {
            "Поле" -> culturesField
            "Сад" -> culturesGarden
            "Теплица" -> culturesGreenhouse
            "Лесопосадка" -> listOf()
            else -> listOf()
        }

        val cultureAdapter = createBlackTextAdapter(cultures)
        cultureSpinner.adapter = cultureAdapter
        cultureSpinner.isEnabled = cultures.isNotEmpty()
    }

    private fun updateDistrictSpinner(region: String) {
        val districts = districtsMap[region] ?: listOf()

        val districtAdapter = createBlackTextAdapter(districts)
        districtSpinner.adapter = districtAdapter
    }

    private fun updateDynamicFields(activityType: String) {
        dynamicFieldsLayout.removeAllViews()

        when (activityType) {
            "Герболог" -> {
                dynamicFieldsLayout.addView(createEditText("Площадь обследуемого участка"))
                dynamicFieldsLayout.addView(createEditText("Количество мест учета"))
                dynamicFieldsLayout.addView(createEditText("Количество сорняков"))
                dynamicFieldsLayout.addView(TextView(this).apply { text = "Сорняк" })
                dynamicFieldsLayout.addView(createSpinnerWithItems(listOf("Повелика полевая", "Молочай лозный", "Гречиха татарская", "Вьюнок полевой", "Гарчак ползучий"), "Сорняк"))
            }
            "Энтомолог" -> {
                dynamicFieldsLayout.addView(createEditText("Площадь обследуемого участка"))
                dynamicFieldsLayout.addView(createEditText("Количество вредителей"))
                dynamicFieldsLayout.addView(TextView(this).apply { text = "Вредитель" })
                dynamicFieldsLayout.addView(createSpinnerWithItems(listOf("Тля", "Рапсовый листоед", "Колорадский жук", "Минирующий пилильщик", "Просяная жужелица"), "Вредитель"))
            }
            "Фитопатолог" -> {
                dynamicFieldsLayout.addView(createEditText("Количество обследуемых растений"))
                dynamicFieldsLayout.addView(createEditText("Количество пораженных растений"))
                dynamicFieldsLayout.addView(createEditText("Степень поражения"))
                dynamicFieldsLayout.addView(TextView(this).apply { text = "Болезнь" })
                dynamicFieldsLayout.addView(createSpinnerWithItems(listOf("Бактериальный ожёг", "Головня хлебных злаков", "Пшеничная головня", "Ржавчина", "Фитофтороз"), "Болезнь"))
            }
        }
    }

    private fun createEditText(hint: String): EditText {
        return EditText(this).apply {
            this.hint = hint
            this.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            this.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8
            }
            this.tag = hint
        }
    }

    private fun createSpinnerWithItems(items: List<String>, tag: String): Spinner {
        val spinner = Spinner(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 8
            }
            this.tag = tag
        }
        val adapter = createBlackTextAdapter(items)
        spinner.adapter = adapter
        return spinner
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            dateButton.text = "$dayOfMonth/${month + 1}/$year"
        }
        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun checkAndRequestPermissions(): Boolean {
        val permissionsNeeded = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        return if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), REQUEST_PERMISSIONS_CODE)
            false
        } else {
            true
        }
    }

    private lateinit var currentPhotoPath: String

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
                    }
                } else {
                    Toast.makeText(this, "Разрешение на доступ к местоположению отклонено", Toast.LENGTH_SHORT).show()
                }
            }
            CAMERA_PERMISSION_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED })) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Необходимы разрешения для камеры и хранения", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_PERMISSIONS_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED })) {
                    // Все разрешения даны
                } else {
                    Toast.makeText(this, "Необходимы все разрешения для корректной работы приложения", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun collectReportData(): Report {
        val dynamicFieldsData = collectDynamicFieldsData()
        return Report(
            id = System.currentTimeMillis(),
            activityType = activityTypeSpinner.selectedItem?.toString() ?: "",
            workType = workTypeSpinner.selectedItem?.toString() ?: "",
            workPlace = workPlaceSpinner.selectedItem?.toString() ?: "",
            culture = cultureSpinner.selectedItem?.toString() ?: "",
            developmentStage = developmentStageSpinner.selectedItem?.toString() ?: "",
            region = regionSpinner.selectedItem?.toString() ?: "",
            district = districtSpinner.selectedItem?.toString() ?: "",
            executor = workerSpinner.selectedItem?.toString() ?: "",
            area = areaEditText.text.toString().toDoubleOrNull() ?: 0.0,
            farmName = farmEditText.text.toString(),
            latitude = selectedLocation?.latitude ?: 0.0,
            longitude = selectedLocation?.longitude ?: 0.0,
            description = descriptionEditText.text.toString(),
            photos = photosList,
            results = calculateResults(),
            date = dateButton.text.toString(),
            dynamicFieldsData = dynamicFieldsData
        )
    }

    private fun collectDynamicFieldsData(): MutableMap<String, String> {
        val data = mutableMapOf<String, String>()
        when (activityTypeSpinner.selectedItem.toString()) {
            "Герболог" -> {
                data["Площадь обследуемого участка"] = dynamicFieldsLayout.findViewWithTag<EditText>("Площадь обследуемого участка")?.text.toString()
                data["Количество мест учета"] = dynamicFieldsLayout.findViewWithTag<EditText>("Количество мест учета")?.text.toString()
                data["Количество сорняков"] = dynamicFieldsLayout.findViewWithTag<EditText>("Количество сорняков")?.text.toString()
                val weedSpinner = dynamicFieldsLayout.findViewWithTag<Spinner>("Сорняк")
                data["Сорняк"] = weedSpinner?.selectedItem?.toString() ?: ""
            }
            "Энтомолог" -> {
                data["Площадь обследуемого участка"] = dynamicFieldsLayout.findViewWithTag<EditText>("Площадь обследуемого участка")?.text.toString()
                data["Количество вредителей"] = dynamicFieldsLayout.findViewWithTag<EditText>("Количество вредителей")?.text.toString()
                val pestSpinner = dynamicFieldsLayout.findViewWithTag<Spinner>("Вредитель")
                data["Вредитель"] = pestSpinner?.selectedItem?.toString() ?: ""
            }
            "Фитопатолог" -> {
                data["Количество обследуемых растений"] = dynamicFieldsLayout.findViewWithTag<EditText>("Количество обследуемых растений")?.text.toString()
                data["Количество пораженных растений"] = dynamicFieldsLayout.findViewWithTag<EditText>("Количество пораженных растений")?.text.toString()
                data["Степень поражения"] = dynamicFieldsLayout.findViewWithTag<EditText>("Степень поражения")?.text.toString()
                val diseaseSpinner = dynamicFieldsLayout.findViewWithTag<Spinner>("Болезнь")
                data["Болезнь"] = diseaseSpinner?.selectedItem?.toString() ?: ""
            }
        }
        return data
    }


    private fun calculateResults(): String {
        val activityType = activityTypeSpinner.selectedItem.toString()
        return when (activityType) {
            "Герболог" -> {
                val area = dynamicFieldsLayout.findViewWithTag<EditText>("Площадь обследуемого участка")?.text.toString().toDoubleOrNull() ?: 0.0
                val locations = dynamicFieldsLayout.findViewWithTag<EditText>("Количество мест учета")?.text.toString().toIntOrNull() ?: 0
                val weeds = dynamicFieldsLayout.findViewWithTag<EditText>("Количество сорняков")?.text.toString().toIntOrNull() ?: 0
                val score = when {
                    weeds <= 1 -> 1
                    weeds <= 3 -> 2
                    weeds <= 5 -> 3
                    else -> 4
                }
                "Баллы: $score"
            }
            "Энтомолог" -> {
                val area = dynamicFieldsLayout.findViewWithTag<EditText>("Площадь обследуемого участка")?.text.toString().toDoubleOrNull() ?: 0.0
                val pests = dynamicFieldsLayout.findViewWithTag<EditText>("Количество вредителей")?.text.toString().toIntOrNull() ?: 0
                "Количество вредителей: $pests"
            }
            "Фитопатолог" -> {
                val total = dynamicFieldsLayout.findViewWithTag<EditText>("Количество обследуемых растений")?.text.toString().toIntOrNull() ?: 0
                val affected = dynamicFieldsLayout.findViewWithTag<EditText>("Количество пораженных растений")?.text.toString().toIntOrNull() ?: 0
                val severity = dynamicFieldsLayout.findViewWithTag<EditText>("Степень поражения")?.text.toString().toDoubleOrNull() ?: 0.0
                val percentage = if (total > 0) (affected / total.toDouble()) * 100 else 0.0
                "Процент пораженных растений: %.2f%%".format(percentage)
            }
            else -> "Нет результатов"
        }
    }

    private fun saveReport(report: Report) {
        val appDir = File(getExternalFilesDir(null), "ReportsApp")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        val xmlFile = File(appDir, "report_${report.id}.xml")
        try {
            val serializer: XmlSerializer = Xml.newSerializer()
            val fos = FileOutputStream(xmlFile)
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

            serializer.startTag("", "Photos")
            for (photoPath in report.photos) {
                serializer.startTag("", "Photo")
                serializer.text(photoPath)
                serializer.endTag("", "Photo")
            }
            serializer.endTag("", "Photos")

            // Сохранение DynamicFieldsData
            serializer.startTag("", "DynamicFieldsData")
            for ((key, value) in report.dynamicFieldsData) {
                serializer.startTag("", "Field")
                serializer.attribute("", "name", key)
                serializer.text(value)
                serializer.endTag("", "Field")
            }
            serializer.endTag("", "DynamicFieldsData")

            serializer.startTag("", "Results")
            serializer.text(report.results)
            serializer.endTag("", "Results")

            serializer.startTag("", "Date")
            serializer.text(report.date)
            serializer.endTag("", "Date")

            serializer.endTag("", "Report")
            serializer.endDocument()

            fos.close()

            Toast.makeText(this, "Отчет сохранен: ${xmlFile.absolutePath}", Toast.LENGTH_LONG).show()
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка при сохранении отчета: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
