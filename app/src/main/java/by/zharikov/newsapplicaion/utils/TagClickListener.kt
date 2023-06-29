package by.zharikov.newsapplicaion.utils

import by.zharikov.newsapplicaion.domain.model.TagModelUi

interface TagClickListener {
    fun onTagClickListener(tagUi: TagModelUi)
}