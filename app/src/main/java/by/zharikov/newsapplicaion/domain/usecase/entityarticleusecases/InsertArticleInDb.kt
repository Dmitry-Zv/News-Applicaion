package by.zharikov.newsapplicaion.domain.usecase.entityarticleusecases

import android.content.SharedPreferences
import by.zharikov.newsapplicaion.domain.model.Article
import by.zharikov.newsapplicaion.domain.repository.ArticleRepository
import by.zharikov.newsapplicaion.domain.repository.SharedArticlesRepository
import javax.inject.Inject

class InsertArticleInDb @Inject constructor(
    private val repository: ArticleRepository,
    private val sharedRepository: SharedArticlesRepository
) {

    suspend operator fun invoke(article: Article) {
        repository.insertArticleInDb(article = article)
        article.title?.let {
            sharedRepository.getPreferences().edit().putBoolean(it, true).apply()
        }
    }
}