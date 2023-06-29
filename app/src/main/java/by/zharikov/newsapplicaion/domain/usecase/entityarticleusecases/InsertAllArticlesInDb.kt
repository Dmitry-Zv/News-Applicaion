package by.zharikov.newsapplicaion.domain.usecase.entityarticleusecases

import by.zharikov.newsapplicaion.domain.model.Article
import by.zharikov.newsapplicaion.domain.repository.ArticleRepository
import by.zharikov.newsapplicaion.domain.repository.SharedArticlesRepository
import javax.inject.Inject

class InsertAllArticlesInDb @Inject constructor(
    private val repository: ArticleRepository,
    private val sharedArticlesRepository: SharedArticlesRepository
) {

    suspend operator fun invoke(articles: List<Article>) {
        repository.insertAllArticles(articles = articles)
        articles.forEach {
            it.title?.let { title ->
                sharedArticlesRepository.getPreferences().edit().putBoolean(title, true).apply()

            }
        }
    }
}