package com.example.a123

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream

class cropDataActivity : AppCompatActivity() {
    private val selectExcelFile = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Uri? = result.data?.data
            data?.let { uri ->
                val inputStream = contentResolver.openInputStream(uri)
                inputStream?.let {
                    val pestData = DataLoader.readPestDataFromExcelStream(it)
                    // Используем загруженные данные для дальнейших действий
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cropactivity_data)

        val btnSelectFile: Button = findViewById(R.id.btnSelectFile)
        btnSelectFile.setOnClickListener {
            selectExcelFile()
        }
    }

    private fun selectExcelFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        selectExcelFile.launch(intent)
    }
}

