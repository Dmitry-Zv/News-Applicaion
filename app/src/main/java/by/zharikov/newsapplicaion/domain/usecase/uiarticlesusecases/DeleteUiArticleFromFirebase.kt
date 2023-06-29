package by.zharikov.newsapplicaion.domain.usecase.uiarticlesusecases

import by.zharikov.newsapplicaion.domain.repository.UiArticlesRepository
import by.zharikov.newsapplicaion.utils.Resource
import javax.inject.Inject

class DeleteUiArticleFromFirebase @Inject constructor(
    private val uiArticlesRepository: UiArticlesRepository
) {

    suspend operator fun invoke(publishedAt: String): Resource<Unit> =
        uiArticlesRepository.deleteUiArticleFromFirebase(publishedAt = publishedAt)
}