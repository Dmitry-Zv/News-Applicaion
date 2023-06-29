package by.zharikov.newsapplicaion.presentation.search

import by.zharikov.newsapplicaion.domain.model.UiArticle

data class SearchFragmentState(
    val state: String,
    val data: List<UiArticle>,
    val error: String?,
    val isLoading: Boolean,
    val uiArticle: UiArticle?,
    val url: String?
) {
    companion object {
        val default = SearchFragmentState(
            state = SearchFragmentStateName.DEFAULT.name,
            data = emptyList(),
            error = null,
            isLoading = false,
            uiArticle = null,
            url = null
        )
    }
}

enum class SearchFragmentStateName {
    DEFAULT, GET_ARTICLES, REFRESH, DETAILED, SHARED_ARTICLE, ERROR
}

