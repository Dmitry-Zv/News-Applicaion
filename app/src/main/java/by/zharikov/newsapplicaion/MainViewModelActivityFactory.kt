package by.zharikov.newsapplicaion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.zharikov.newsapplicaion.repository.ArticleEntityRepository
import java.lang.IllegalArgumentException

class MainViewModelActivityFactory(private val articleEntityRepository: ArticleEntityRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            MainViewModel(this.articleEntityRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel No Found")
        }
    }
}