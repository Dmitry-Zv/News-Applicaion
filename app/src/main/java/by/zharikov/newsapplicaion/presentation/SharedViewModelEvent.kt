package by.zharikov.newsapplicaion.presentation

import by.zharikov.newsapplicaion.domain.model.UiArticle

sealed class SharedViewModelEvent {
    data class ShowArticlesBadgeCounter(val uiArticle: UiArticle) : SharedViewModelEvent()
    data class ShowError(val msg: String) : SharedViewModelEvent()
    object ResetBadgeCounter : SharedViewModelEvent()
    data class InitBadgeCounter(val badgeCounterKey: String) : SharedViewModelEvent()
    data class CountItemFavourite(val data: Int) : SharedViewModelEvent()
}