package by.zharikov.newsapplicaion.presentation.main

import by.zharikov.newsapplicaion.domain.model.TagModelUi
import by.zharikov.newsapplicaion.domain.model.UiArticle

sealed class MainFragmentEvent {

    object Refresh : MainFragmentEvent()

    object Default : MainFragmentEvent()

    data class OnCellClick(val uiArticle: UiArticle) : MainFragmentEvent()

    data class OnSaveIconClick(val uiArticle: UiArticle) : MainFragmentEvent()

    data class OnShareIconClick(val url: String) : MainFragmentEvent()

    data class OnTagClick(val tagModelUi: TagModelUi) : MainFragmentEvent()

    data class GetArticles(val countryCode: String) : MainFragmentEvent()

    data class ShowError(val msg: String) : MainFragmentEvent()


}