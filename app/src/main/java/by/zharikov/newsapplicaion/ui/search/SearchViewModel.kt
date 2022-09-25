package by.zharikov.newsapplicaion.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.zharikov.newsapplicaion.data.model.NewsModel
import by.zharikov.newsapplicaion.repository.NewsRepository
import kotlinx.coroutines.launch

class SearchViewModel(private val newsRepository: NewsRepository) : ViewModel() {
    private val _newsViewModel = MutableLiveData<NewsModel>()
    val newsModel: LiveData<NewsModel>
        get() = _newsViewModel
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage
    private val pageNumber = 1

    fun getNews(q: String) {
        viewModelScope.launch {
            val response = newsRepository.newsGetEverything(q, pageNumber)
            if (response.isSuccessful) {
                _newsViewModel.postValue(response.body())
            } else {
                _errorMessage.postValue(response.message())
            }
        }

    }

}