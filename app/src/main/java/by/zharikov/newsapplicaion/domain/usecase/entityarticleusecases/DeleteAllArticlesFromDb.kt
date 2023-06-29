package by.zharikov.newsapplicaion.domain.usecase.entityarticleusecases

import by.zharikov.newsapplicaion.domain.repository.ArticleRepository
import by.zharikov.newsapplicaion.domain.repository.SharedArticlesRepository
import javax.inject.Inject

class DeleteAllArticlesFromDb @Inject constructor(
    private val repository: ArticleRepository,
    private val sharedArticlesRepository: SharedArticlesRepository
) {

    suspend operator fun invoke() {
        repository.deleteAllArticlesFromDb()
        sharedArticlesRepository.getPreferences().edit().clear().apply()
    }
}