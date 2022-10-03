package by.zharikov.newsapplicaion.data.model


import java.io.Serializable

data class NewsModel(
    val articles: List<Article> = listOf(),
    val status: String = "",
    val totalResults: Int = 0
) : Serializable