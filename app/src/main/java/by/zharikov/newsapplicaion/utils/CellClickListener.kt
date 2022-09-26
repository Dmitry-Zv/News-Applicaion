package by.zharikov.newsapplicaion.utils

import by.zharikov.newsapplicaion.data.model.Article

interface CellClickListener {
    fun onCellClickListener(article: Article)
}