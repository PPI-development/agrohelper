package com.example.a123

import android.content.Context
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream

data class PestData(
    val year: Int,
    val pestCount: Float,
    val temperature: Float,
    val precipitation: Float
)

object DataLoader {
    // Чтение данных из файла Excel через InputStream (например, при выборе файла пользователем)
    fun readPestDataFromExcelStream(inputStream: InputStream): List<PestData> {
        val pestDataList = mutableListOf<PestData>()
        val workbook = WorkbookFactory.create(inputStream)
        val sheet = workbook.getSheetAt(0)

        for (row in sheet) {
            if (row.rowNum == 0) continue // Пропускаем заголовок

            try {
                val year = row.getCell(0)?.numericCellValue?.toInt() ?: continue
                val pestCount = row.getCell(1)?.numericCellValue?.toFloat() ?: continue
                val temperature = row.getCell(2)?.numericCellValue?.toFloat() ?: 0f
                val precipitation = row.getCell(3)?.numericCellValue?.toFloat() ?: 0f

                // Добавляем только валидные данные
                if (year > 0 && pestCount >= 0) {
                    pestDataList.add(PestData(year, pestCount, temperature, precipitation))
                }
            } catch (e: Exception) {
                e.printStackTrace() // Игнорируем строки с ошибками
            }
        }

        workbook.close()
        return pestDataList
    }

    // Пример метода для чтения данных из ресурса raw
    fun readPestDataFromExcel(context: Context, resourceId: Int): List<PestData> {
        val inputStream: InputStream = context.resources.openRawResource(resourceId)
        return readPestDataFromExcelStream(inputStream)
    }
}
