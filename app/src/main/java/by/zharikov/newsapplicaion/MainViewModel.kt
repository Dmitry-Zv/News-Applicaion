package by.zharikov.newsapplicaion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.zharikov.newsapplicaion.data.model.EntityArticle
import by.zharikov.newsapplicaion.repository.ArticleEntityRepository
import kotlinx.coroutines.launch

class MainViewModel(private val articleEntityRepository: ArticleEntityRepository) : ViewModel() {
    private val _entityListArticle = MutableLiveData<List<EntityArticle>>()
    val entityListArticle: LiveData<List<EntityArticle>>
        get() = _entityListArticle

    fun deleteAllArticle() {
        viewModelScope.launch {
            articleEntityRepository.repDeleteAllArticle()
        }
    }

    fun getAllArticle() {
        viewModelScope.launch {
            val entityList = articleEntityRepository.repGetAllArticles()
            _entityListArticle.postValue(entityList)
        }
    }

}