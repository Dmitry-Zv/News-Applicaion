package by.zharikov.newsapplicaion.usecase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class ArticlePreferencesViewModelFactory(private val articlePreferencesUseCase: ArticlePreferencesUseCase) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ArticlePreferencesViewModel::class.java)) {
            ArticlePreferencesViewModel(this.articlePreferencesUseCase) as T
        } else {
            throw IllegalArgumentException("ViewModel No Found")
        }
    }
}