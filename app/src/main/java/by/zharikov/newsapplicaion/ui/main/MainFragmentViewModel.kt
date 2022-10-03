package by.zharikov.newsapplicaion.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.zharikov.newsapplicaion.data.model.EntityArticle
import by.zharikov.newsapplicaion.data.model.NewsModel
import by.zharikov.newsapplicaion.repository.ArticleEntityRepository
import by.zharikov.newsapplicaion.repository.NewsRepository
import kotlinx.coroutines.launch

class MainFragmentViewModel(
    private val newsRepository: NewsRepository,
    private val articleEntityRepository: ArticleEntityRepository
) : ViewModel() {

    private val _newsLiveData = MutableLiveData<NewsModel>()
    val newLiveData: LiveData<NewsModel>
        get() = _newsLiveData
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage
    private val pageNumber = 1

    fun getNew(countryCode: String) {
        viewModelScope.launch {

            val response = newsRepository.newsGetTopHeadlines(countryCode, pageNumber)

            if (response.isSuccessful) {
                _newsLiveData.postValue(response.body())


            } else _errorMessage.postValue(response.message())
        }
    }

    fun insertArticle(entityArticle: EntityArticle) {
        viewModelScope.launch {

            articleEntityRepository.repInsertArticle(entityArticle)
        }

    }

    fun deleteArticle(title: String) {
        viewModelScope.launch {
            articleEntityRepository.repDeleteArticle(title = title)

        }
    }
}

