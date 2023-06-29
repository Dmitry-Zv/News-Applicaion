package by.zharikov.newsapplicaion.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.zharikov.newsapplicaion.connectivity.MyState
import by.zharikov.newsapplicaion.domain.model.Article
import by.zharikov.newsapplicaion.domain.model.UiArticle
import by.zharikov.newsapplicaion.domain.usecase.badgecounterusecases.BadgeCounterUseCases
import by.zharikov.newsapplicaion.presentation.common.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArticlesCounterState(
    val data: List<String> = emptyList(),
    val msg: String? = null
)

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val badgeCounterUseCases: BadgeCounterUseCases
) : ViewModel(), Event<SharedViewModelEvent> {
    private val _state = MutableLiveData<MyState>()
    val state: LiveData<MyState>
        get() = _state
    private val _articlesState = MutableStateFlow(ArticlesCounterState())
    val articlesState = _articlesState.asStateFlow()
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

    private var badgeCounter: String = ""


    override fun onEvent(event: SharedViewModelEvent) {
        when (event) {
            SharedViewModelEvent.ResetBadgeCounter -> resetCounter()
            is SharedViewModelEvent.ShowArticlesBadgeCounter -> performArticleBadgeCounter(uiArticle = event.uiArticle)
            is SharedViewModelEvent.ShowError -> showError(msg = event.msg)
            is SharedViewModelEvent.InitBadgeCounter -> initBadgeCounter(badgeCounterKey = event.badgeCounterKey)
            is SharedViewModelEvent.CountItemFavourite -> setCountItemFav(countItemSave = event.data)
        }
    }


    private fun setCountItemFav(countItemSave: Int) {
        viewModelScope.launch {
            _countItemSave.emit(countItemSave)
        }
    }

    fun setState(state: MyState) {
        _state.value = state

    }

    private fun performArticleBadgeCounter(uiArticle: UiArticle) {
        viewModelScope.launch {


            if (!uiArticle.isSave) {
                uiArticle.article.title?.let { title ->
                    val listOfArticlesName = _articlesState.value.data + title
                    _articlesState.value = _articlesState.value.copy(data = listOfArticlesName)
                    badgeCounterUseCases.setBadgeCounter(
                        listOfArticlesTitle = listOfArticlesName,
                        badgeCounterKey = badgeCounter
                    )
                } ?: showError(msg = "Title is null...")
            } else {
                uiArticle.article.title?.let { title ->
                    if (_articlesState.value.data.isNotEmpty() &&
                        _articlesState.value.data.contains(title)
                    ) {
                        val listOfArticlesName = _articlesState.value.data - title
                        _articlesState.value = _articlesState.value.copy(data = listOfArticlesName)
                        badgeCounterUseCases.setBadgeCounter(
                            listOfArticlesTitle = listOfArticlesName,
                            badgeCounterKey = badgeCounter
                        )
                    }
                } ?: showError(msg = "Title is null")

            }

        }
    }


    private fun resetCounter() {
        _articlesState.value = ArticlesCounterState()
        badgeCounterUseCases.resetBadgeCounter(badgeCounterKey = badgeCounter)
    }

    fun setStateIsCheckedForPosition0(isChecked: Boolean) {
        _isCheckedPosition0.value = isChecked
    }


    fun setStateIsCheckedForPosition1(isChecked: Boolean) {
        _isCheckedPosition1.value = isChecked
    }


    private fun initBadgeCounter(badgeCounterKey: String) {
        val listOfArticlesTitle =
            badgeCounterUseCases.getBadgeCounter(badgeCounterKey = badgeCounterKey)
        badgeCounter = badgeCounterKey
        _articlesState.value = _articlesState.value.copy(data = listOfArticlesTitle)
    }

    private fun showError(msg: String) {
        _articlesState.value = _articlesState.value.copy(msg = msg)
    }


}