package com.example.a123

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import java.io.IOException

class ChatActivity : AppCompatActivity() {

    private lateinit var chatContainer: LinearLayout
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var photoButton: Button
    private lateinit var chatScrollView: ScrollView
    private lateinit var chatbot: ChatBot  // Экземпляр текстового бота
    private lateinit var imageAnalyzer: ImageAnalysisModel  // Экземпляр модели анализа изображений
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private val CAMERA_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        chatContainer = findViewById(R.id.chatContainer)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)
        photoButton = findViewById(R.id.photoButton)
        chatScrollView = findViewById(R.id.chatScrollView)

        // Инициализация бота и модели анализа изображений
        chatbot = ChatBot(this)
        imageAnalyzer = ImageAnalysisModel(this)

        // Активация кнопки отправки при вводе текста
        messageEditText.addTextChangedListener {
            sendButton.isEnabled = it.toString().isNotEmpty()
        }

        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString()
            if (messageText.isNotBlank()) {
                addMessageToChat(messageText, isUser = true)
                messageEditText.text.clear()

                // Анализ текста через бота
                val botResponse = chatbot.getResponse(messageText, this@ChatActivity)
                addMessageToChat(botResponse, isUser = false)
                scrollToBottom()
            }
        }

        photoButton.setOnClickListener {
            showImageSourceDialog()
        }
    }

    // Диалог выбора источника изображения (камера или галерея)
    private fun showImageSourceDialog() {
        val options = arrayOf("Камера", "Галерея")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Выберите источник изображения")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            // Запрашиваем разрешение на камеру
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            // Запуск камеры
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun openGallery() {
        val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Разрешение на камеру отклонено", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    addImageToChat(imageBitmap, isUser = true)

                    // Анализ изображения через модель
                    val analysisResult = imageAnalyzer.analyzeImage(imageBitmap)
                    addMessageToChat(analysisResult, isUser = false)
                    scrollToBottom()
                }
                REQUEST_IMAGE_PICK -> {
                    val selectedImageUri: Uri? = data?.data
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
                        addImageToChat(bitmap, isUser = true)

                        // Анализ изображения через модель
                        val analysisResult = imageAnalyzer.analyzeImage(bitmap)
                        addMessageToChat(analysisResult, isUser = false)
                        scrollToBottom()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    // Метод для добавления текстового сообщения в чат
    private fun addMessageToChat(message: String, isUser: Boolean) {
        val textView = TextView(this)
        textView.text = message
        textView.setPadding(16, 16, 16, 16)
        textView.setBackgroundResource(if (isUser) R.drawable.user_message_background else R.drawable.bot_message_background)

        // Настройка выравнивания для пользователя и бота
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)

        // Сообщения пользователя справа, сообщения бота слева
        layoutParams.gravity = if (isUser) Gravity.END else Gravity.START

        textView.layoutParams = layoutParams
        chatContainer.addView(textView)
    }

    // Метод для добавления изображения в чат
    private fun addImageToChat(bitmap: Bitmap, isUser: Boolean) {
        val imageView = ImageView(this)
        imageView.setImageBitmap(bitmap)

        // Установка максимальных размеров для изображения 200x200
        imageView.layoutParams = LinearLayout.LayoutParams(
            300,  // Максимальная ширина изображения
            300   // Максимальная высота изображения
        ).apply {
            // Дополнительные параметры для правильного отображения
            gravity = if (isUser) Gravity.END else Gravity.START
        }

        // Создаем контейнер для изображения, чтобы применить фон, как к сообщению
        val frameLayout = FrameLayout(this)
        frameLayout.setPadding(16, 16, 16, 16)
        frameLayout.setBackgroundResource(if (isUser) R.drawable.user_message_background else R.drawable.bot_message_background)

        val frameLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        frameLayoutParams.setMargins(16, 16, 16, 16)
        frameLayoutParams.gravity = if (isUser) Gravity.END else Gravity.START

        frameLayout.layoutParams = frameLayoutParams
        frameLayout.addView(imageView)

        // Добавление в контейнер чата
        chatContainer.addView(frameLayout)
        scrollToBottom()
    }


    // Прокрутка чата к последнему сообщению
    private fun scrollToBottom() {
        chatScrollView.post {
            chatScrollView.fullScroll(View.FOCUS_DOWN)
        }
    }
}
