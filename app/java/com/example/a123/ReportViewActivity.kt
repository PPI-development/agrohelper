package com.example.a123

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.File
import android.content.Intent
import android.widget.Button
import android.util.Log

class ReportViewActivity : AppCompatActivity() {

    private lateinit var report: Report
    private lateinit var xmlFilePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_view)

        // Получаем ссылки на элементы интерфейса
        val reportTitleTextView: TextView = findViewById(R.id.reportTitleTextView)
        val reportTextView: TextView = findViewById(R.id.reportTextView)
        val photosLayout: LinearLayout = findViewById(R.id.photosLayout)
        val buttonEdit: Button = findViewById(R.id.buttonEdit)

        // Получаем путь к XML-файлу отчёта из Intent
        xmlFilePath = intent.getStringExtra("xmlFilePath") ?: return

        // Парсим XML-файл и получаем объект Report
        report = ReportUtils.parseXmlReport(xmlFilePath)

        // Устанавливаем заголовок отчёта
        reportTitleTextView.text = "Отчёт №${report.id}"

        // Форматируем и устанавливаем текст отчёта с жирными заголовками
        val formattedText = formatReportText(report)
        reportTextView.text = formattedText

        // Загружаем и отображаем фотографии
        displayPhotos(report.photos, photosLayout)

        // Назначаем обработчик нажатия на кнопку "Редактировать"
        buttonEdit.setOnClickListener {
            openEditActivity()
        }
    }

    /**
     * Форматирует текст отчёта с жирными заголовками.
     *
     * @param report Объект Report с данными отчёта.
     * @return Отформатированный текст с HTML-разметкой.
     */
    private fun formatReportText(report: Report): Spannable {
        val builder = StringBuilder()

        // Формируем строку с HTML-разметкой для жирных заголовков
        builder.append("<b>ID:</b> ${report.id}<br/>")
        builder.append("<b>Вид деятельности:</b> ${report.activityType}<br/>")
        builder.append("<b>Вид работы:</b> ${report.workType}<br/>")
        builder.append("<b>Место проведения работ:</b> ${report.workPlace}<br/>")
        builder.append("<b>Культура:</b> ${report.culture}<br/>")
        builder.append("<b>Фаза развития:</b> ${report.developmentStage}<br/>")
        builder.append("<b>Регион:</b> ${report.region}<br/>")
        builder.append("<b>Район:</b> ${report.district}<br/>")
        builder.append("<b>Исполнитель:</b> ${report.executor}<br/>")
        builder.append("<b>Площадь:</b> ${report.area} га<br/>")
        builder.append("<b>Название хозяйства:</b> ${report.farmName}<br/>")
        builder.append("<b>Широта:</b> ${report.latitude}<br/>")
        builder.append("<b>Долгота:</b> ${report.longitude}<br/>")
        builder.append("<b>Описание:</b> ${report.description}<br/>")
        builder.append("<b>Результаты:</b> ${report.results}<br/>")
        builder.append("<b>Дата:</b> ${report.date}<br/>")
        builder.append("<b>Результаты исследования:</b><br/>")

        // Добавляем динамические поля с жирными заголовками
        for ((key, value) in report.dynamicFieldsData) {
            builder.append("<b>$key:</b> $value<br/>")
        }

        // Преобразуем HTML-строку в Spannable для отображения в TextView
        val spannedText: Spannable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(builder.toString(), Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(builder.toString())
        } as Spannable

        return spannedText
    }

    /**
     * Загружает и отображает фотографии отчёта.
     *
     * @param photos Список путей к фотографиям.
     * @param photosLayout Линейный макет для размещения ImageView.
     */
    private fun displayPhotos(photos: List<String>, photosLayout: LinearLayout) {
        photosLayout.removeAllViews() // Очищаем предыдущие фотографии, если есть

        for (photoPath in photos) {
            val imageView = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    600 // Ограничиваем высоту изображения
                ).apply {
                    bottomMargin = 16 // Отступ снизу между фотографиями
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setPadding(8, 8, 8, 8)
            }

            // Загружаем изображение с помощью Glide
            Glide.with(this)
                .load(photoPath)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .override(600, 600) // Размеры изображения
                        .centerCrop()
                )
                .into(imageView)

            photosLayout.addView(imageView)
        }
    }

    private fun openEditActivity() {
        val intent = Intent(this, EditReportActivity::class.java)
        intent.putExtra("xmlFilePath", xmlFilePath)
        startActivityForResult(intent, REQUEST_EDIT_REPORT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EDIT_REPORT && resultCode == RESULT_OK) {
            // Загрузить обновленный отчет
            report = ReportUtils.parseXmlReport(xmlFilePath)
            // Обновить отображение отчета
            val formattedText = formatReportText(report)
            findViewById<TextView>(R.id.reportTextView).text = formattedText
            displayPhotos(report.photos, findViewById(R.id.photosLayout))
        }
    }

    companion object {
        private const val REQUEST_EDIT_REPORT = 1002
    }
}
