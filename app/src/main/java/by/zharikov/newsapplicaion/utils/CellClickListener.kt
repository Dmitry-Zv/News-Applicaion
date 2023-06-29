package by.zharikov.newsapplicaion.utils

import by.zharikov.newsapplicaion.domain.model.UiArticle

interface CellClickListener {
    fun onCellClickListener(uiArticle: UiArticle)
}