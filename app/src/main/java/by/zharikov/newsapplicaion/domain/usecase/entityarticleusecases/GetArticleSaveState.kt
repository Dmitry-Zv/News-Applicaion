package by.zharikov.newsapplicaion.domain.usecase.entityarticleusecases

import by.zharikov.newsapplicaion.domain.model.Article
import by.zharikov.newsapplicaion.domain.repository.SharedArticlesRepository
import javax.inject.Inject

class GetArticleSaveState @Inject constructor(
    private val articlesRepository: SharedArticlesRepository
) {
    operator fun invoke(title: String): Boolean =
        articlesRepository.getPreferences().getBoolean(title, false)


}