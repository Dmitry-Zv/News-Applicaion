package by.zharikov.newsapplicaion.presentation.main

import by.zharikov.newsapplicaion.domain.model.Article
import by.zharikov.newsapplicaion.domain.model.UiArticle

data class MainFragmentState(
    val state: String,
    val data: List<UiArticle>,
    val error: String?,
    val isLoading: Boolean,
    val category: String?,
    val uiArticle: UiArticle?,
    val url: String?
) {
    companion object {
        val default = MainFragmentState(
            state = MainFragmentStateName.DEFAULT.name,
            data = emptyList(),
            error = null,
            isLoading = false,
            category = null,
            uiArticle = null,
            url = null
        )
    }
}

enum class MainFragmentStateName {
    DEFAULT, GET_ARTICLES, REFRESH, DETAILED, SHARED_ARTICLE, ERROR
}

