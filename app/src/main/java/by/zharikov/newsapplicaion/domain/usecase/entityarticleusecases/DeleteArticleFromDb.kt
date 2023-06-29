package by.zharikov.newsapplicaion.domain.usecase.entityarticleusecases

import by.zharikov.newsapplicaion.domain.repository.ArticleRepository
import by.zharikov.newsapplicaion.domain.repository.SharedArticlesRepository
import javax.inject.Inject

class DeleteArticleFromDb @Inject constructor(
    private val repository: ArticleRepository,
    private val sharedArticlesRepository: SharedArticlesRepository
) {

    suspend operator fun invoke(title: String) {
        repository.deleteArticleFromDb(title = title)
        sharedArticlesRepository.getPreferences().edit().remove(title).apply()
    }
}