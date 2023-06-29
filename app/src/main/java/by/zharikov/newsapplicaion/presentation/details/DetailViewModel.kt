package by.zharikov.newsapplicaion.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.zharikov.newsapplicaion.domain.model.Article
import by.zharikov.newsapplicaion.domain.model.UiArticle
import by.zharikov.newsapplicaion.domain.usecase.entityarticleusecases.EntityArticleUseCases
import by.zharikov.newsapplicaion.domain.usecase.uiarticlesusecases.UiArticlesUseCases
import by.zharikov.newsapplicaion.presentation.common.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val entityArticleUseCases: EntityArticleUseCases,
    private val uiArticlesUseCases: UiArticlesUseCases
) : ViewModel(), Event<DetailFragmentEvent> {

    private val _state = MutableStateFlow(DetailFragmentState.default)
    val state = _state.asStateFlow()
    override fun onEvent(event: DetailFragmentEvent) {
        when (event) {
            DetailFragmentEvent.Default -> performDefault()
            is DetailFragmentEvent.OnShareIconClick -> shareArticle(url = event.url)
            is DetailFragmentEvent.ShowError -> showError(msg = event.msg)
            DetailFragmentEvent.PressBack -> pressBack()
            is DetailFragmentEvent.PressWeb -> pressWeb(article = event.article)
            is DetailFragmentEvent.PressSaveIcon -> pressSaveIcon(uiArticle = event.uiArticle)
        }
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
            _state.value = _state.value.copy(
                state = DetailFragmentStateName.SAVED.name,
                isSaved = !uiArticle.isSave
            )

        }
    }

    private fun pressWeb(article: Article) {
        _state.value =
            _state.value.copy(state = DetailFragmentStateName.WEB.name, article = article)
    }

    private fun pressBack() {
        _state.value = _state.value.copy(state = DetailFragmentStateName.BACK.name)
    }

    private fun showError(msg: String) {
        _state.value = _state.value.copy(state = DetailFragmentStateName.ERROR.name, error = msg)
    }

    private fun shareArticle(url: String) {
        _state.value =
            _state.value.copy(state = DetailFragmentStateName.SHARED_ARTICLE.name, url = url)
    }

    private fun performDefault() {
        _state.value = DetailFragmentState.default
    }
}