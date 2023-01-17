package by.zharikov.newsapplicaion.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.zharikov.newsapplicaion.connectivity.MyState
import by.zharikov.newsapplicaion.data.model.Article
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {
    private val _state = MutableLiveData<MyState>()
    val state: LiveData<MyState>
        get() = _state
    private val _counter = MutableSharedFlow<Int>()
    val counter = _counter.asSharedFlow()
    private val _articles = MutableStateFlow(listOf<Article>())
    val articles = _articles.asStateFlow()
    private val _isCheckedPosition0 = MutableLiveData<Boolean>()
    val isCheckedPosition0: LiveData<Boolean>
        get() = _isCheckedPosition0
    private val _isCheckedPosition1 = MutableLiveData<Boolean>()
    val isCheckedPosition1: LiveData<Boolean>
        get() = _isCheckedPosition1

    private val _countItemSave = MutableSharedFlow<Int>()
    val countItemSave = _countItemSave.asSharedFlow()

    fun setCountItemFav(countItemSave: Int) {
        viewModelScope.launch {
            _countItemSave.emit(countItemSave)
        }
    }

    fun setState(state: MyState) {
        _state.value = state

    }

    fun setCounter(counter: Int) {
        viewModelScope.launch {
            _counter.emit(counter)
        }
    }

    fun setStateIsCheckedForPosition0(isChecked: Boolean) {
        _isCheckedPosition0.value = isChecked
    }


    fun setStateIsCheckedForPosition1(isChecked: Boolean) {
        _isCheckedPosition1.value = isChecked
    }


}