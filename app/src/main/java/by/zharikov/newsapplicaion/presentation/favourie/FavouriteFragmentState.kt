package by.zharikov.newsapplicaion.presentation.favourie

import by.zharikov.newsapplicaion.domain.model.Article
import by.zharikov.newsapplicaion.domain.model.UiArticle

data class FavouriteFragmentState(
    val state: String,
    val data: List<UiArticle>,
    val error: String?,
    val isLoading: Boolean,
    val uiArticle: UiArticle?,
    val url: String?
) {
    companion object {
        val default = FavouriteFragmentState(
            state = FavouriteFragmentStateName.DEFAULT.name,
            data = emptyList(),
            error = null,
            isLoading = false,
            uiArticle = null,
            url = null
        )
    }
}


enum class FavouriteFragmentStateName {
    DEFAULT, GET_ARTICLES, DETAILED, SHARED_ARTICLE, ERROR
}

