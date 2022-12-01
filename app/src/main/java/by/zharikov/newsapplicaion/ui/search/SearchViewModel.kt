package by.zharikov.newsapplicaion.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.zharikov.newsapplicaion.data.model.EntityArticle
import by.zharikov.newsapplicaion.data.model.NewsModel
import by.zharikov.newsapplicaion.repository.ArticleEntityRepository
import by.zharikov.newsapplicaion.repository.NewsRepository
import kotlinx.coroutines.launch

class SearchViewModel(
    private val newsRepository: NewsRepository,
    private val articleEntityRepository: ArticleEntityRepository
) : ViewModel() {
    private val _newsViewModel = MutableLiveData<NewsModel>()
    val newsModel: LiveData<NewsModel>
        get() = _newsViewModel
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage
    private val pageNumber = 1

    fun getNews(q: String) {
        viewModelScope.launch {
            try {
                val response = newsRepository.newsGetEverything(q, pageNumber)
                if (response.isSuccessful) {
                    _newsViewModel.postValue(response.body())
                }
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
            }

        }
    }

    fun getArticle(countryCode: String) {
        viewModelScope.launch {
            try {
                val response = newsRepository.newsGetTopHeadlines(countryCode, pageNumber)

                if (response.isSuccessful) {
                    _newsViewModel.postValue(response.body())
                }
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
            }

        }
    }


    fun insertArticle(article: EntityArticle) {
        viewModelScope.launch {

            articleEntityRepository.repInsertArticle(article = article)
        }

    }

    fun deleteArticle(title: String) {
        viewModelScope.launch {

            articleEntityRepository.repDeleteArticle(title = title)
        }
    }

}