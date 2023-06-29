package by.zharikov.newsapplicaion.presentation.favourie

import by.zharikov.newsapplicaion.domain.model.UiArticle

sealed class FavouriteFragmentEvent {
    object Default : FavouriteFragmentEvent()
    data class OnDeleteIcon(val title: String, val publishedAt: String) : FavouriteFragmentEvent()
    object GetArticle : FavouriteFragmentEvent()
    data class OnCellClick(val uiArticle: UiArticle) : FavouriteFragmentEvent()
    data class OnShareIconClick(val url: String) : FavouriteFragmentEvent()
    data class ShowError(val msg: String) : FavouriteFragmentEvent()

}