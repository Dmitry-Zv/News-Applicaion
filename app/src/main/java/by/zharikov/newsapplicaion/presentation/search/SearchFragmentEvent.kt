package by.zharikov.newsapplicaion.presentation.search

import by.zharikov.newsapplicaion.domain.model.UiArticle

sealed class SearchFragmentEvent {

    object Refresh : SearchFragmentEvent()

    object Default : SearchFragmentEvent()

    data class OnCellClick(val uiArticle: UiArticle) : SearchFragmentEvent()

    data class OnSaveIconClick(val uiArticle: UiArticle) : SearchFragmentEvent()

    data class OnShareIconClick(val url: String) : SearchFragmentEvent()

    data class GetArticles(val query: String) : SearchFragmentEvent()

    data class ShowError(val msg: String) : SearchFragmentEvent()


}