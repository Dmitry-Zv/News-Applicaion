package by.zharikov.newsapplicaion.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.zharikov.newsapplicaion.repository.ArticleEntityRepository
import by.zharikov.newsapplicaion.repository.NewsRepository
import java.lang.IllegalArgumentException

class SearchViewModelFactory(
    private val newsRepository: NewsRepository,
    private val articleEntityRepository: ArticleEntityRepository
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            SearchViewModel(this.newsRepository, this.articleEntityRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel No Found")
        }
    }
}