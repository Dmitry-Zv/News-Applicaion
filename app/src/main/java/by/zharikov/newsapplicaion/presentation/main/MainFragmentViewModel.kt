package by.zharikov.newsapplicaion.presentation.main

import by.zharikov.newsapplicaion.domain.model.UiArticle
import androidx.lifecycle.*
import by.zharikov.newsapplicaion.domain.model.TagModelUi
import by.zharikov.newsapplicaion.domain.usecase.entityarticleusecases.EntityArticleUseCases
import by.zharikov.newsapplicaion.domain.usecase.newsusecases.NewsUseCases
import by.zharikov.newsapplicaion.domain.usecase.tagusecases.TagUseCases
import by.zharikov.newsapplicaion.domain.usecase.uiarticlesusecases.UiArticlesUseCases
import by.zharikov.newsapplicaion.presentation.common.Event
import by.zharikov.newsapplicaion.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainFragmentViewModel @Inject constructor(
    private val newsUseCases: NewsUseCases,
    private val tagUseCases: TagUseCases,
    private val entityArticleUseCases: EntityArticleUseCases,
    private val uiArticlesUseCases: UiArticlesUseCases
) : ViewModel(), Event<MainFragmentEvent> {

    private val _state = MutableStateFlow(MainFragmentState.default)
    val state = _state.asStateFlow()
    private val _tagUiList = MutableStateFlow(listOf<TagModelUi>())
    val tagUiList = _tagUiList.asStateFlow()


    override fun onEvent(event: MainFragmentEvent) {
        when (event) {
            is MainFragmentEvent.GetArticles -> getArticles(countryCode = event.countryCode)
            is MainFragmentEvent.OnCellClick -> performDetailed(uiArticle = event.uiArticle)
            is MainFragmentEvent.OnSaveIconClick -> pressSaveIcon(uiArticle = event.uiArticle)
            is MainFragmentEvent.OnShareIconClick -> shareArticle(url = event.url)
            is MainFragmentEvent.OnTagClick -> setTag(tagModelUi = event.tagModelUi)
            MainFragmentEvent.Refresh -> performRefresh()
            is MainFragmentEvent.ShowError -> showError(msg = event.msg)
            MainFragmentEvent.Default -> performDefault()
        }
    }

    private fun getArticlesSaveState(title: String?): Boolean =
        title?.let {
            entityArticleUseCases.getArticleSaveState(title = it)
        } ?: false


    private fun performDefault() {
        _state.value = MainFragmentState.default
    }

    private fun showError(msg: String) {
        _state.value = _state.value.copy(state = MainFragmentStateName.ERROR.name, error = msg)
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

                when (val result =
                    uiArticlesUseCases.saveUiArticlesToFirebase(article = uiArticle.article)) {
                    is Resource.Error -> {
                        showError(result.msg)
                    }
                    is Resource.Success -> {}
                }
            }

        }

    }


    private fun shareArticle(url: String) {
        _state.value =
            _state.value.copy(state = MainFragmentStateName.SHARED_ARTICLE.name, url = url)
    }

    private fun performDetailed(uiArticle: UiArticle) {
        _state.value =
            _state.value.copy(state = MainFragmentStateName.DETAILED.name, uiArticle = uiArticle)
    }

    private fun performRefresh() {
        _state.value = _state.value.copy(state = MainFragmentStateName.REFRESH.name)
    }

    private fun setTag(tagModelUi: TagModelUi) {
        viewModelScope.launch {
            tagUseCases.setTags(tagModelUi = tagModelUi)

        }
        getArticles("us")
    }

    private fun getArticles(countryCode: String) {
        _state.value =
            _state.value.copy(state = MainFragmentStateName.GET_ARTICLES.name, isLoading = true)
        viewModelScope.launch {


            tagUseCases.getTags()
                .onEach { tagUiList ->
                    _tagUiList.value = tagUiList
                    tagUiList.find { tagModelUi ->
                        tagModelUi.isClicked
                    }?.tagModel?.tagName?.let {
                        when (val result = newsUseCases.getTopHeadLinesCategory(
                            country = countryCode,
                            category = it
                        )) {
                            is Resource.Error -> _state.value = _state.value.copy(
                                state = MainFragmentStateName.GET_ARTICLES.name,
                                data = emptyList(),
                                error = result.msg,
                                isLoading = false,
                                category = null,
                                uiArticle = null
                            )
                            is Resource.Success -> {
                                val uiArticle = UiArticle.mapFromListOfArticleToListOfUiArticle(
                                    articles = result.data,
                                    articleSaveState = { title ->
                                        getArticlesSaveState(
                                            title = title
                                        )
                                    }
                                )
                                _state.value = _state.value.copy(
                                    state = MainFragmentStateName.GET_ARTICLES.name,
                                    data = uiArticle,
                                    error = null,
                                    isLoading = false,
                                    category = countryCode,
                                    uiArticle = null
                                )
                            }
                        }
                    } ?: when (val result = newsUseCases.getTopHeadLinesArticle(
                        country = countryCode,
                        pageNumber = 1
                    )) {
                        is Resource.Error -> _state.value = _state.value.copy(
                            state = MainFragmentStateName.GET_ARTICLES.name,
                            data = emptyList(),
                            error = result.msg,
                            isLoading = false,
                            category = null,
                            uiArticle = null
                        )
                        is Resource.Success -> {
                            val uiArticle = UiArticle.mapFromListOfArticleToListOfUiArticle(
                                articles = result.data,
                                articleSaveState = { title ->
                                    getArticlesSaveState(title = title)
                                }
                            )
                            _state.value = _state.value.copy(
                                state = MainFragmentStateName.GET_ARTICLES.name,
                                data = uiArticle,
                                error = null,
                                isLoading = false,
                                category = null,
                                uiArticle = null
                            )
                        }
                    }


                }.launchIn(viewModelScope)
        }
    }
}



