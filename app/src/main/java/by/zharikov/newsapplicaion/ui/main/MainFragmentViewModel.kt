package by.zharikov.newsapplicaion.ui.main

import androidx.lifecycle.*
import by.zharikov.newsapplicaion.data.model.*
import by.zharikov.newsapplicaion.repository.TagRepository
import by.zharikov.newsapplicaion.usecase.article_retrofit_use_case.ArticleRetrofitUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class MainFragmentViewModel(
    private val articleRetrofitUseCase: ArticleRetrofitUseCase,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState = _uiState.asStateFlow()
    private val pageNumber = 1
    private var _tagUiList = MutableStateFlow(listOf<TagModelUi>())
    val tagUiList = _tagUiList.asStateFlow()

    init {
        getTagUiList()
        viewModelScope.launch {
            articleRetrofitUseCase.resultState.collectLatest { resultState ->
                when (resultState) {
                    is by.zharikov.newsapplicaion.usecase.article_retrofit_use_case.Result.SuccessTopHeadlinesArticles -> _uiState.value =
                        UiState.ShowArticles(resultState.articles)
                    is by.zharikov.newsapplicaion.usecase.article_retrofit_use_case.Result.SuccessArticleGetTopHeadLinesCategory -> _uiState.value =
                        UiState.ShowArticles(resultState.articles)
                    is by.zharikov.newsapplicaion.usecase.article_retrofit_use_case.Result.Error -> _uiState.value =
                        UiState.Error(resultState.exception)
                    else -> {}
                }
            }
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

    fun getArticlesByCategory(countryCode: String, category: String) {
        viewModelScope.launch {
            articleRetrofitUseCase.invokeGetTopHeadLinesCategoryArticle(
                country = countryCode,
                category = category
            )
        }
    }


    fun setTagUiList(tagModelUi: TagModelUi) {
        viewModelScope.launch {
            val tagUiList = tagRepository.setTags(tagModelUi)
            _tagUiList.value = tagUiList
        }
    }

    private fun getTagUiList() {
        viewModelScope.launch {
            val tagUiList = tagRepository.getTagsBySharedPreferences()
            _tagUiList.value = tagUiList
        }
    }


}

