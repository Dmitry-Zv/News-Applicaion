package by.zharikov.newsapplicaion.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.zharikov.newsapplicaion.usecase.article_retrofit_use_case.ArticleRetrofitUseCase
import java.lang.IllegalArgumentException

class SearchViewModelFactory(
    private val articleRetrofitUseCase: ArticleRetrofitUseCase
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            SearchViewModel(this.articleRetrofitUseCase) as T
        } else {
            throw IllegalArgumentException("ViewModel No Found")
        }
    }
}