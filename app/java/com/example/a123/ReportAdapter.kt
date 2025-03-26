package com.example.a123

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.File

class ReportAdapter(
    private var reports: List<Report>,
    private val onItemClick: (Report) -> Unit,
    private val onItemLongClick: (Report) -> Unit
) : RecyclerView.Adapter<ReportViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view, onItemClick, onItemLongClick)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.bind(report)

        // Загрузка изображения из XML-файла
        if (report.photos.isNotEmpty()) {
            val photoPath = report.photos[0]
            val photoFile = File(photoPath)

            if (photoFile.exists()) {
                // Используем Glide для загрузки изображения
                Glide.with(holder.itemView.context)
                    .load(photoFile)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.placeholder_image)
                            .override(800, 600)
                            .centerCrop()
                    )
                    .into(holder.imageViewPhoto)
            } else {
                // Если файл не найден, устанавливаем плейсхолдер
                holder.imageViewPhoto.setImageResource(R.drawable.placeholder_image)
            }
        } else {
            // Если фотографий нет, устанавливаем плейсхолдер
            holder.imageViewPhoto.setImageResource(R.drawable.placeholder_image)
        }
    }

    override fun getItemCount(): Int = reports.size

    /**
     * Обновляет данные в адаптере и уведомляет об изменениях.
     *
     * @param newReports Новый список отчётов.
     */
    fun updateData(newReports: List<Report>) {
        reports = newReports
        notifyDataSetChanged()
    }
}
