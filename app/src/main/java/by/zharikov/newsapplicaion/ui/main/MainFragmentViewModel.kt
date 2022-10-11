package by.zharikov.newsapplicaion.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.zharikov.newsapplicaion.data.model.EntityArticle
import by.zharikov.newsapplicaion.data.model.NewsModel
import by.zharikov.newsapplicaion.data.model.TagModelUi
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

    private val _tagUiList = MutableLiveData<List<TagModelUi>>()
    val tagUiList: LiveData<List<TagModelUi>>
        get() = _tagUiList


    fun getNew(countryCode: String) {
        viewModelScope.launch {
            try {
                val response = newsRepository.newsGetTopHeadlines(countryCode, pageNumber)

                if (response.isSuccessful) {
                    _newsLiveData.postValue(response.body())
                }
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
            }

        }
    }

    fun getNewByCategory(countryCode: String, category: String) {
        viewModelScope.launch {
            try {
                val response = newsRepository.newGetTopHeadLinesCategory(
                    country = countryCode,
                    category = category
                )
                if (response.isSuccessful) {
                    _newsLiveData.postValue(response.body())
                }
            } catch (e: Exception) {
                _errorMessage.postValue(e.message)
            }
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

    fun setTagUiList(tagUiList: List<TagModelUi>) {
        _tagUiList.value = tagUiList
    }


}

