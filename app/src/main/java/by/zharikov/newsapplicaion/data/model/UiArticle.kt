package by.zharikov.newsapplicaion.data.model


data class UiArticle(
    val article: Article,
    var isLiked: Boolean = false
)