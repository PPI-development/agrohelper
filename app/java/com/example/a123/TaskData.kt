package com.example.a123
import java.util.Calendar

data class TaskData(val description: String, val timeInMillis: Long) {
    override fun toString(): String {
        val calendar = Calendar.getInstance().apply { timeInMillis = this@TaskData.timeInMillis }
        val date = android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", calendar)
        return "$description — $date"
    }
}


