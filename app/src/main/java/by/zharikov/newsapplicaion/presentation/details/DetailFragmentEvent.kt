package by.zharikov.newsapplicaion.presentation.details

import by.zharikov.newsapplicaion.domain.model.Article
import by.zharikov.newsapplicaion.domain.model.UiArticle

sealed class DetailFragmentEvent {

    object Default : DetailFragmentEvent()
    data class OnShareIconClick(val url: String) : DetailFragmentEvent()
    data class ShowError(val msg: String) : DetailFragmentEvent()
    data class PressWeb(val article: Article) : DetailFragmentEvent()
    object PressBack : DetailFragmentEvent()
    data class PressSaveIcon(val uiArticle: UiArticle):DetailFragmentEvent()

}