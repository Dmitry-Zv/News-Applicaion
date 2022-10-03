package by.zharikov.newsapplicaion.ui.favourie

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.zharikov.newsapplicaion.data.model.EntityArticle
import by.zharikov.newsapplicaion.repository.ArticleEntityRepository
import kotlinx.coroutines.launch

class FavouriteViewModel(private val articleEntityRepository: ArticleEntityRepository) :
    ViewModel() {

    private var articles = listOf<EntityArticle>()
    private val _saveData = MutableLiveData<List<EntityArticle>>()
    val saveData: LiveData<List<EntityArticle>>
        get() = _saveData

    fun getArticles(){
        viewModelScope.launch {
            articles = articleEntityRepository.repGetAllArticles()
            for (article in articles) {
                Log.d("GetArt", article.title.toString())

            }
            _saveData.postValue(articles)
        }
    }


    fun deleteArticle(title: String) {
        viewModelScope.launch {

            articleEntityRepository.repDeleteArticle(title = title)
            getArticles()

        }
    }

}