package com.example.a123

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.File
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ImageAnalysisModel(context: Context) {

    private var interpreter: Interpreter

    init {
        val modelFile = loadModelFile(context, "plant_disease_model.tflite")
        interpreter = Interpreter(modelFile)
    }

    private fun loadModelFile(context: Context, modelFileName: String): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelFileName)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun analyzeImage(bitmap: Bitmap): String {
        // Измените размер изображения на тот, который ожидается моделью (например, 224x224)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

        // Преобразование Bitmap в тензор (float array)
        val inputTensor = convertBitmapToInputTensor(resizedBitmap)

        // Подготовка тензора для вывода (если модель возвращает 38 классов)
        val output = Array(1) { FloatArray(38) }  // Здесь указано 38 классов, как показано в ошибке

        // Запуск анализа изображения через модель
        interpreter.run(inputTensor, output)

        // Обработка результатов
        return processOutput(output[0])
    }

    private fun processOutput(output: FloatArray): String {
        // Находим индекс с максимальным значением в выходном массиве
        val maxIndex = output.indices.maxByOrNull { output[it] } ?: -1

        // Проверяем, что индекс найден и существует в списке классов
        if (maxIndex != -1 && maxIndex < Companion.classNames.size) {
            val className = Companion.classNames[maxIndex]

            // Получаем русскоязычное название класса, если оно существует
            return Companion.russianClassNames[className] ?: "Обнаружено: $className"
        }

        return "Не удалось определить заболевание"
    }




    private fun convertBitmapToInputTensor(bitmap: Bitmap): Array<Array<Array<FloatArray>>> {
        // Создаем тензор размером [1, 224, 224, 3]
        val input = Array(1) { Array(224) { Array(224) { FloatArray(3) } } }

        // Преобразуем каждый пиксель в три канала (R, G, B)
        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val pixel = bitmap.getPixel(x, y)
                input[0][y][x][0] = (pixel shr 16 and 0xFF) / 255.0f  // Красный канал
                input[0][y][x][1] = (pixel shr 8 and 0xFF) / 255.0f   // Зеленый канал
                input[0][y][x][2] = (pixel and 0xFF) / 255.0f         // Синий канал
            }
        }

        return input
    }




    private fun preprocessBitmap(bitmap: Bitmap): Array<FloatArray> {
        val input = Array(1) { FloatArray(224 * 224 * 3) }
        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val pixel = bitmap.getPixel(x, y)
                input[0][(y * 224 + x) * 3] = ((pixel shr 16) and 0xFF) / 255.0f
                input[0][(y * 224 + x) * 3 + 1] = ((pixel shr 8) and 0xFF) / 255.0f
                input[0][(y * 224 + x) * 3 + 2] = (pixel and 0xFF) / 255.0f
            }
        }
        return input
    }

    companion object {
        val classNames = listOf(
            "Apple___Apple_scab", "Apple___Black_rot", "Apple___Cedar_apple_rust", "Apple___healthy",
            "Blueberry___healthy", "Cherry_(including_sour)___Powdery_mildew", "Cherry_(including_sour)___healthy",
            "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot", "Corn_(maize)___Common_rust_",
            "Corn_(maize)___Northern_Leaf_Blight", "Corn_(maize)___healthy", "Grape___Black_rot",
            "Grape___Esca_(Black_Measles)", "Grape___Leaf_blight_(Isariopsis_Leaf_Spot)", "Grape___healthy",
            "Orange___Haunglongbing_(Citrus_greening)", "Peach___Bacterial_spot", "Peach___healthy",
            "Pepper,_bell___Bacterial_spot", "Pepper,_bell___healthy", "Potato___Early_blight",
            "Potato___Late_blight", "Potato___healthy", "Raspberry___healthy", "Soybean___healthy",
            "Squash___Powdery_mildew", "Strawberry___Leaf_scorch", "Strawberry___healthy",
            "Tomato___Bacterial_spot", "Tomato___Early_blight", "Tomato___Late_blight", "Tomato___Leaf_Mold",
            "Tomato___Septoria_leaf_spot", "Tomato___Spider_mites Two-spotted_spider_mite",
            "Tomato___Target_Spot", "Tomato___Tomato_Yellow_Leaf_Curl_Virus", "Tomato___Tomato_mosaic_virus",
            "Tomato___healthy"
        )

        // Маппинг классов на их русскоязычные описания
        val russianClassNames = mapOf(
            "Apple___Apple_scab" to "Обнаружена Яблоневая парша",
            "Apple___Black_rot" to "Обнаружена Яблоневая черная гниль",
            "Apple___Cedar_apple_rust" to "Обнаружена Яблоневая ржавчина",
            "Apple___healthy" to "Яблоня здорова",
            "Blueberry___healthy" to "Черника здорова",
            "Cherry_(including_sour)___Powdery_mildew" to "Обнаружена Мучнистая роса на вишне",
            "Cherry_(including_sour)___healthy" to "Вишня здорова",
            "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot" to "Обнаружено Серое пятно на кукурузе",
            "Corn_(maize)___Common_rust_" to "Обнаружена Обычная ржавчина на кукурузе",
            "Corn_(maize)___Northern_Leaf_Blight" to "Обнаружена Северная пятнистость листьев кукурузы",
            "Corn_(maize)___healthy" to "Кукуруза здорова",
            "Grape___Black_rot" to "Обнаружена Черная гниль винограда",
            "Grape___Esca_(Black_Measles)" to "Обнаружен Эска (черная корь) винограда",
            "Grape___Leaf_blight_(Isariopsis_Leaf_Spot)" to "Обнаружена Листовая пятнистость винограда",
            "Grape___healthy" to "Виноград здоров",
            "Orange___Haunglongbing_(Citrus_greening)" to "Обнаружен Гаунглонгбинг (цитрусовая зелень)",
            "Peach___Bacterial_spot" to "Обнаружено Бактериальное пятно на персике",
            "Peach___healthy" to "Персик здоров",
            "Potato___Early_blight" to "Обнаружена Ранняя фитофтора картофеля",
            "Potato___Late_blight" to "Обнаружена Поздняя фитофтора картофеля",
            "Potato___healthy" to "Картофель здоров",
            "Raspberry___healthy" to "Малина здорова",
            "Soybean___healthy" to "Соевые бобы здоровы",
            "Squash___Powdery_mildew" to "Обнаружена Мучнистая роса на тыкве",
            "Strawberry___Leaf_scorch" to "Обнаружено Листовое обжигание на клубнике",
            "Strawberry___healthy" to "Клубника здорова",
            "Tomato___Bacterial_spot" to "Обнаружено Бактериальное пятно на томатах",
            "Tomato___Early_blight" to "Обнаружена Ранняя фитофтора томатов",
            "Tomato___Late_blight" to "Обнаружена Поздняя фитофтора томатов",
            "Tomato___Leaf_Mold" to "Обнаружена Листовая плесень на томатах",
            "Tomato___Septoria_leaf_spot" to "Обнаружена Септориозная пятнистость на томатах",
            "Tomato___Spider_mites Two-spotted_spider_mite" to "Обнаружен Паутинный клещ на томатах",
            "Tomato___Target_Spot" to "Обнаружена Таргетная пятнистость на томатах",
            "Tomato___Tomato_Yellow_Leaf_Curl_Virus" to "Обнаружен Вирус желтой курчавости листьев томатов",
            "Tomato___Tomato_mosaic_virus" to "Обнаружен Вирус мозаики томатов",
            "Tomato___healthy" to "Томаты здоровы"
        )
    }
}

