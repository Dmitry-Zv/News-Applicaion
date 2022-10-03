package by.zharikov.newsapplicaion.utils

import androidx.recyclerview.widget.DiffUtil
import by.zharikov.newsapplicaion.data.model.UiArticle

class ArticlesDiffUtil(
    private val oldItem: MutableList<UiArticle>,
    private val newItem: MutableList<UiArticle>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldItem.size
    }

    override fun getNewListSize(): Int {
        return newItem.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItem[oldItemPosition].article.url == newItem[newItemPosition].article.url
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItem[oldItemPosition] == newItem[newItemPosition]
    }
}