package by.zharikov.newsapplicaion.ui.favourie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.zharikov.newsapplicaion.repository.ArticleEntityRepository
import java.lang.IllegalArgumentException

class FavouriteViewModelFactory(private val articleEntityRepository: ArticleEntityRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(FavouriteViewModel::class.java)) {
            FavouriteViewModel(this.articleEntityRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel No Found")
        }
    }
}