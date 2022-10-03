package by.zharikov.newsapplicaion.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.zharikov.newsapplicaion.data.model.EntityArticle
import by.zharikov.newsapplicaion.repository.ArticleEntityRepository
import kotlinx.coroutines.launch

class DetailViewModel(private val articleEntityRepository: ArticleEntityRepository) : ViewModel() {


    fun insertArticle(entityArticle: EntityArticle) {
        viewModelScope.launch {
            articleEntityRepository.repInsertArticle(article = entityArticle)
        }
    }

    fun deleteArticle(title: String) {
        viewModelScope.launch {
            articleEntityRepository.repDeleteArticle(title = title)
        }
    }

}