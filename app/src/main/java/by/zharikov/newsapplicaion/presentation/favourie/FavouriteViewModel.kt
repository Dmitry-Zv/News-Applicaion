package by.zharikov.newsapplicaion.presentation.favourie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.zharikov.newsapplicaion.domain.model.UiArticle
import by.zharikov.newsapplicaion.domain.usecase.entityarticleusecases.EntityArticleUseCases
import by.zharikov.newsapplicaion.domain.usecase.uiarticlesusecases.UiArticlesUseCases
import by.zharikov.newsapplicaion.presentation.common.Event
import by.zharikov.newsapplicaion.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouriteViewModel @Inject constructor(
    private val entityArticleUseCases: EntityArticleUseCases,
    private val uiArticlesUseCases: UiArticlesUseCases
) : ViewModel(), Event<FavouriteFragmentEvent> {

    private val _state = MutableStateFlow(FavouriteFragmentState.default)
    val state = _state.asStateFlow()


    override fun onEvent(event: FavouriteFragmentEvent) {
        when (event) {
            FavouriteFragmentEvent.Default -> performRefresh()
            is FavouriteFragmentEvent.GetArticle -> getArticles()
            is FavouriteFragmentEvent.OnCellClick -> performDetailed(uiArticle = event.uiArticle)
            is FavouriteFragmentEvent.OnDeleteIcon -> deleteArticle(
                title = event.title,
                publishedAt = event.publishedAt
            )
            is FavouriteFragmentEvent.OnShareIconClick -> shareArticle(url = event.url)
            is FavouriteFragmentEvent.ShowError -> showError(msg = event.msg)
        }
    }

    private fun showError(msg: String) {
        _state.value = _state.value.copy(state = FavouriteFragmentStateName.ERROR.name, error = msg)
    }

    private fun shareArticle(url: String) {
        _state.value =
            _state.value.copy(state = FavouriteFragmentStateName.SHARED_ARTICLE.name, url = url)
    }

    private fun deleteArticle(title: String, publishedAt: String) {
        viewModelScope.launch {
            entityArticleUseCases.deleteArticleFromDb(title = title)
            uiArticlesUseCases.deleteUiArticleFromFirebase(publishedAt = publishedAt)
        }
    }

    private fun performDetailed(uiArticle: UiArticle) {
        _state.value =
            _state.value.copy(
                state = FavouriteFragmentStateName.DETAILED.name,
                uiArticle = uiArticle
            )
    }

    private fun getArticles() {
        _state.value = _state.value.copy(isLoading = true)
        when (val result = entityArticleUseCases.getAllArticlesFromDb()) {
            is Resource.Error -> _state.value = _state.value.copy(
                state = FavouriteFragmentStateName.ERROR.name,
                data = emptyList(),
                error = result.msg,
                isLoading = false,
                url = null,
                uiArticle = null
            )
            is Resource.Success -> {
                result.data.onEach {
                    val uiArticles = UiArticle.mapFromListOfArticleToListOfUiArticle(
                        articles = it,
                        articleSaveState = {
                            true
                        }
                    )
                    _state.value = _state.value.copy(
                        state = FavouriteFragmentStateName.GET_ARTICLES.name,
                        data = uiArticles.reversed(),
                        error = null,
                        isLoading = true,
                        url = null,
                        uiArticle = null
                    )
                }.launchIn(viewModelScope)

            }


        }
    }

    private fun performRefresh() {
        _state.value = _state.value.copy(state = FavouriteFragmentStateName.DEFAULT.name)
    }
}