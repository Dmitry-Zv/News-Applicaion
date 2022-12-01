package by.zharikov.newsapplicaion.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import by.zharikov.newsapplicaion.connectivity.MyState
import by.zharikov.newsapplicaion.data.model.Article

class SharedViewModel : ViewModel() {
    val state = MutableLiveData<MyState>()
    val counter = MutableLiveData<Int>()
    val articles = MutableLiveData<List<Article>>()
    private val _isCheckedPosition0 = MutableLiveData<Boolean>()
    val isCheckedPosition0: LiveData<Boolean>
        get() = _isCheckedPosition0
    private val _isCheckedPosition1 = MutableLiveData<Boolean>()
    val isCheckedPosition1: LiveData<Boolean>
        get() = _isCheckedPosition1

    private val _countItemSave = MutableLiveData<Int>()
    val countItemSave: LiveData<Int>
        get() = _countItemSave

    fun setCountItemFav(countItemSave: Int) {
        _countItemSave.value = countItemSave
    }

    fun setState(state: MyState) {
        this.state.value = state
    }

    fun setCounter(counter: Int) {
        this.counter.value = counter
    }

    fun setUiListArticle(articles: List<Article>) {
        this.articles.value = articles
    }

    fun setStateIsCheckedForPosition0(isChecked: Boolean) {
        _isCheckedPosition0.value = isChecked
    }


    fun setStateIsCheckedForPosition1(isChecked: Boolean) {
        _isCheckedPosition1.value = isChecked
    }


}