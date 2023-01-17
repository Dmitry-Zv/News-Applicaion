package by.zharikov.newsapplicaion.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.zharikov.newsapplicaion.data.model.UiState
import by.zharikov.newsapplicaion.usecase.article_retrofit_use_case.Result
import by.zharikov.newsapplicaion.usecase.article_retrofit_use_case.ArticleRetrofitUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchViewModel(
    private val articleRetrofitUseCase: ArticleRetrofitUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState = _uiState.asStateFlow()

    private val pageNumber = 1

    init {
        viewModelScope.launch {
            articleRetrofitUseCase.resultState.collectLatest { result ->
                when (result) {
                    is Result.SuccessArticleGetEverything -> _uiState.value =
                        UiState.ShowArticles(articles = result.articles)
                    is Result.SuccessTopHeadlinesArticles -> _uiState.value =
                        UiState.ShowArticles(articles = result.articles)
                    is Result.Error -> _uiState.value =
                        UiState.Error(exception = result.exception)
                    else -> {}
                }
            }
        }

    }

    fun getArticleQuery(q: String) {
        viewModelScope.launch {
            articleRetrofitUseCase.invokeGetEverythingArticle(q = q, pageNumber = pageNumber)
        }
    }

    fun getArticles(countryCode: String) {
        viewModelScope.launch {
            articleRetrofitUseCase.invokeGetTopHeadLinesArticle(
                country = countryCode,
                pageNumber = pageNumber
            )
        }
    }


}