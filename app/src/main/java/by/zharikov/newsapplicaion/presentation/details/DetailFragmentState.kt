package by.zharikov.newsapplicaion.presentation.details

import by.zharikov.newsapplicaion.domain.model.Article


data class DetailFragmentState(
    val state: String,
    val error: String?,
    val article: Article?,
    val url: String?,
    val isSaved: Boolean
) {
    companion object {
        val default = DetailFragmentState(
            state = DetailFragmentStateName.DEFAULT.name,
            error = null,
            article = null,
            url = null,
            isSaved = false
        )
    }
}

enum class DetailFragmentStateName {
    DEFAULT, SHARED_ARTICLE, ERROR, WEB, BACK, SAVED
}
