package by.zharikov.newsapplicaion.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.zharikov.newsapplicaion.repository.TagRepository
import by.zharikov.newsapplicaion.usecase.article_retrofit_use_case.ArticleRetrofitUseCase
import java.lang.IllegalArgumentException

class MainViewModelFactory(
    private val articleRetrofitUseCase: ArticleRetrofitUseCase,
    private val tagRepository: TagRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MainFragmentViewModel::class.java)) {
            MainFragmentViewModel(
                this.articleRetrofitUseCase,
                this.tagRepository
            ) as T
        } else {
            throw IllegalArgumentException("ViewModel No Found")
        }
    }
}