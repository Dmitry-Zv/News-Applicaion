package by.zharikov.newsapplicaion.usecase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.zharikov.newsapplicaion.data.model.Article
import by.zharikov.newsapplicaion.data.model.UiArticle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


sealed class UiState {
    object ShowEmptyScreen : UiState()
    class ShowUiArticle(val uiArticles: MutableList<UiArticle>) : UiState()
    object Initial : UiState()
    object Loading : UiState()
    class Error(val e: Exception) : UiState()
    class ShowMessage(val message: String) : UiState()


}

class ArticlePreferencesViewModel(private val articlePreferencesUseCase: ArticlePreferencesUseCase) :
    ViewModel() {

    private val _uiStateFlow =
        MutableStateFlow<UiState>(UiState.Initial)
    val uiStateFlow = _uiStateFlow.asStateFlow()

    init {
        getAllArticles()
    }

    fun getAllArticles() {

        viewModelScope.launch {
            _uiStateFlow.value = UiState.Loading
            when (val result = articlePreferencesUseCase.invokeGetAllArticles()) {
                is Result.UiArticles -> _uiStateFlow.value =
                    UiState.ShowUiArticle(result.uiArticles)

                is Result.EmptyArticles -> _uiStateFlow.value = UiState.ShowEmptyScreen

                is Result.Error -> _uiStateFlow.value = UiState.Error(result.exception)
                else -> {}
            }

        }
    }

    fun addArticle(article: Article) {
        viewModelScope.launch {
            _uiStateFlow.value = UiState.Loading
            when (val result = articlePreferencesUseCase.invokeSetSavedArticle(article)) {
                is Result.Success -> _uiStateFlow.value = UiState.ShowMessage(result.success)
                is Result.Error -> _uiStateFlow.value = UiState.Error(result.exception)
                else -> {}
            }
        }
    }

    fun deleteEntityArticle(article: Article) {
        viewModelScope.launch {
            _uiStateFlow.value = UiState.Loading
            when (val result = articlePreferencesUseCase.invokeDeleteSavedArticle(article)) {
                is Result.Success -> _uiStateFlow.value = UiState.ShowMessage(result.success)
                is Result.Error -> _uiStateFlow.value = UiState.Error(result.exception)
                else -> {}
            }
        }
    }

    fun deleteAllArticle() {
        viewModelScope.launch {
            _uiStateFlow.value = UiState.Loading
            when (val result = articlePreferencesUseCase.invokeDeleteAllSavedArticle()) {
                is Result.EmptyArticles -> _uiStateFlow.value = UiState.ShowEmptyScreen
                is Result.Error -> _uiStateFlow.value = UiState.Error(result.exception)
                else -> {}
            }
        }
    }


}