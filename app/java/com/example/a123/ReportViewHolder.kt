package com.example.a123

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class ReportViewHolder(
    itemView: View,
    private val onItemClick: (Report) -> Unit,
    private val onItemLongClick: (Report) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    val imageViewPhoto: ImageView = itemView.findViewById(R.id.imageViewPhoto)
    val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
    val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)

    fun bind(report: Report) {
        // Установка фото
        if (report.photos.isNotEmpty()) {
            val photoFile = File(report.photos[0])
            if (photoFile.exists()) {
                imageViewPhoto.setImageBitmap(android.graphics.BitmapFactory.decodeFile(photoFile.absolutePath))
            } else {
                imageViewPhoto.setImageResource(R.drawable.placeholder_image)
            }
        } else {
            imageViewPhoto.setImageResource(R.drawable.placeholder_image)
        }

        // Установка текста
        textViewTitle.text = report.workType
        textViewDate.text = report.date

        // Обработка клика
        itemView.setOnClickListener {
            onItemClick(report)
        }

        // Обработка долгого нажатия
        itemView.setOnLongClickListener {
            onItemLongClick(report)
            true
        }
    }
}
