package com.example.a123

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.*
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import kotlin.math.abs
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import android.text.Html
import android.text.method.LinkMovementMethod

class News : AppCompatActivity() {

    private lateinit var newsRecyclerView: RecyclerView
    private lateinit var newsAdapter: NewsAdapter

    // Переменные для отслеживания свайпов
    private var x1: Float = 0.0f
    private var x2: Float = 0.0f
    private var y1: Float = 0.0f
    private var y2: Float = 0.0f
    private val minDistance = 150  // Минимальное расстояние для распознавания свайпа

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        // Инициализация RecyclerView
        newsRecyclerView = findViewById(R.id.newsRecyclerView)
        newsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Устанавливаем слушатель касаний на основной layout, чтобы отслеживать свайпы
        val mainLayout: View = findViewById(R.id.news_main_layout)
        mainLayout.setOnTouchListener { _, event -> handleTouch(event) }

        // Начальная загрузка данных
        fetchNews()
    }

    // Функция для обработки свайпов
    private fun handleTouch(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                x1 = event.x
                y1 = event.y
            }
            MotionEvent.ACTION_UP -> {
                x2 = event.x
                y2 = event.y
                val deltaX = x2 - x1
                val deltaY = y2 - y1

                if (abs(deltaX) > abs(deltaY)) {
                    // Горизонтальный свайп
                    if (abs(deltaX) > minDistance) {
                        if (x2 > x1) {
                            // Свайп вправо - возвращаемся в MainActivity
                            openMainActivity()
                        }
                    }
                }
            }
        }
        return true
    }

    // Метод для возврата в MainActivity
    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    // Функция для получения новостей из Google News RSS
    private fun fetchNews() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://news.google.com/")
            .addConverterFactory(SimpleXmlConverterFactory.create())  // Используем SimpleXML для обработки RSS
            .build()

        val api = retrofit.create(NewsApiService::class.java)
        val call = api.getNews(
            query = "сельское хозяйство Казахстан",
            language = "ru",
            region = "RU",
            ceid = "RU:ru"
        )

        call.enqueue(object : Callback<RSSResponse> {
            override fun onResponse(call: Call<RSSResponse>, response: Response<RSSResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val newsList = response.body()?.channel?.items ?: emptyList()  // Если channel или items = null, вернем пустой список
                    newsAdapter = NewsAdapter(newsList)
                    newsRecyclerView.adapter = newsAdapter
                } else {
                    Log.e("News", "Ошибка при получении данных: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<RSSResponse>, t: Throwable) {
                Log.e("News", "Не удалось загрузить данные", t)
            }
        })
    }
}

// Интерфейс для API
interface NewsApiService {
    @GET("rss/search")
    fun getNews(
        @Query("q") query: String,
        @Query("hl") language: String,
        @Query("gl") region: String,
        @Query("ceid") ceid: String
    ): Call<RSSResponse>
}

// Модель для RSS-ответа
@Root(name = "rss", strict = false)
data class RSSResponse @JvmOverloads constructor(
    @field:Element(name = "channel")
    var channel: Channel? = null
)

@Root(name = "channel", strict = false)
data class Channel @JvmOverloads constructor(
    @field:ElementList(inline = true, required = false)
    var items: MutableList<Item> = mutableListOf()  // Изменяемый список
)

@Root(name = "item", strict = false)
data class Item @JvmOverloads constructor(
    @field:Element(name = "title")
    var title: String? = null,

    @field:Element(name = "link")
    var link: String? = null,

    @field:Element(name = "description", required = false)
    var description: String? = null
)


// Адаптер для RecyclerView с цветными блоками новостей
class NewsAdapter(private val newsList: List<Item>) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): NewsViewHolder {
        val itemView = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.news_item_layout, parent, false)
        return NewsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = newsList[position]
        holder.titleView.text = article.title
        holder.descriptionView.text = article.description

        // Удаление HTML тега ссылки из описания (если оно содержит HTML)
        if (article.description != null) {
            holder.descriptionView.text = Html.fromHtml(article.description)
        }

        // Устанавливаем кликабельность для открытия ссылки в браузере
        holder.itemView.setOnClickListener {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse(article.link)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return newsList.size
    }

    class NewsViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val titleView: android.widget.TextView = itemView.findViewById(R.id.newsTitle)
        val descriptionView: android.widget.TextView = itemView.findViewById(R.id.newsDescription)
    }
}
