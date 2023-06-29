package by.zharikov.newsapplicaion.domain.model


data class NewsModel(
    val articles: List<Article> = listOf(),
    val status: String = "",
    val totalResults: Int = 0
)