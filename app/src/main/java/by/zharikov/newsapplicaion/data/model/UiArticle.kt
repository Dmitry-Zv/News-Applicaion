package by.zharikov.newsapplicaion.data.model


data class UiArticle(
    var article: Article,
    var isLiked: Boolean
) {
    constructor() : this(
        Article(
            0, "", "", "", "", null, "", "", ""
        ), false
    )
}