package by.zharikov.newsapplicaion.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.zharikov.newsapplicaion.domain.model.UiArticle
import by.zharikov.newsapplicaion.domain.usecase.entityarticleusecases.EntityArticleUseCases
import by.zharikov.newsapplicaion.domain.usecase.newsusecases.NewsUseCases
import by.zharikov.newsapplicaion.domain.usecase.uiarticlesusecases.UiArticlesUseCases
import by.zharikov.newsapplicaion.presentation.common.Event
import by.zharikov.newsapplicaion.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val newsUseCases: NewsUseCases,
    private val entityArticleUseCases: EntityArticleUseCases,
    private val uiArticlesUseCases: UiArticlesUseCases
) : ViewModel(), Event<SearchFragmentEvent> {

    private val _state = MutableStateFlow(SearchFragmentState.default)
    val state = _state.asStateFlow()

    init {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            when (val result = newsUseCases.getTopHeadLinesArticle("us", 1)) {
                is Resource.Error -> _state.value = _state.value.copy(
                    state = SearchFragmentStateName.ERROR.name,
                    data = emptyList(),
                    error = result.msg,
                    isLoading = false,
                    uiArticle = null,
                    url = null
                )
                is Resource.Success -> {
                    val uiArticle = UiArticle.mapFromListOfArticleToListOfUiArticle(
                        articles = result.data,
                        articleSaveState = { title ->
                            getArticlesSaveState(title = title)
                        }
                    )
                    _state.value = _state.value.copy(
                        state = SearchFragmentStateName.GET_ARTICLES.name,
                        data = uiArticle,
                        error = null,
                        isLoading = false,
                        uiArticle = null,
                        url = null
                    )
                }
            }
        }
    }

    override fun onEvent(event: SearchFragmentEvent) {
        when (event) {
            is SearchFragmentEvent.GetArticles -> getArticles(query = event.query)
            is SearchFragmentEvent.OnCellClick -> performDetailed(uiArticle = event.uiArticle)
            is SearchFragmentEvent.OnSaveIconClick -> pressSaveIcon(uiArticle = event.uiArticle)
            is SearchFragmentEvent.OnShareIconClick -> shareArticle(url = event.url)
            SearchFragmentEvent.Refresh -> performRefresh()
            is SearchFragmentEvent.ShowError -> showError(msg = event.msg)
            SearchFragmentEvent.Default -> performDefault()
        }
    }

    private fun getArticlesSaveState(title: String?): Boolean =
        title?.let {
            entityArticleUseCases.getArticleSaveState(title = it)
        } ?: false

    private fun performDefault() {
        _state.value = SearchFragmentState.default
    }

    private fun showError(msg: String) {
        _state.value = _state.value.copy(state = SearchFragmentStateName.ERROR.name, error = msg)
    }

    private fun performRefresh() {
        _state.value = _state.value.copy(state = SearchFragmentStateName.REFRESH.name)
    }

    private fun shareArticle(url: String) {
        _state.value =
            _state.value.copy(state = SearchFragmentStateName.SHARED_ARTICLE.name, url = url)
    }

    private fun pressSaveIcon(uiArticle: UiArticle) {
        viewModelScope.launch {
            if (uiArticle.isSave) {
                uiArticle.article.title?.let { title ->
                    entityArticleUseCases.deleteArticleFromDb(title = title)
                    uiArticle.article.publishedAt?.let { publishedAt ->
                        uiArticlesUseCases.deleteUiArticleFromFirebase(publishedAt = publishedAt)

                    } ?: showError(msg = "Published time is null...")

                } ?: showError(msg = "Title of article is null...")

            } else {
                entityArticleUseCases.insertArticleInDb(article = uiArticle.article)
                uiArticlesUseCases.saveUiArticlesToFirebase(article = uiArticle.article)
            }
        }
    }

    private fun performDetailed(uiArticle: UiArticle) {
        _state.value =
            _state.value.copy(state = SearchFragmentStateName.DETAILED.name, uiArticle = uiArticle)
    }

    private fun getArticles(query: String) {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            when (val result = newsUseCases.getEverythingArticles(q = query, pageNumber = 1)) {
                is Resource.Error -> _state.value = _state.value.copy(
                    state = SearchFragmentStateName.ERROR.name,
                    data = emptyList(),
                    error = result.msg,
                    isLoading = false,
                    uiArticle = null,
                    url = null
                )
                is Resource.Success -> {
                    val uiArticle = UiArticle.mapFromListOfArticleToListOfUiArticle(
                        articles = result.data,
                        articleSaveState = { title ->
                            getArticlesSaveState(title = title)
                        }
                    )
                    _state.value = _state.value.copy(
                        state = SearchFragmentStateName.GET_ARTICLES.name,
                        data = uiArticle,
                        error = null,
                        isLoading = false,
                        uiArticle = null,
                        url = null
                    )
                }
            }
        }
    }


}