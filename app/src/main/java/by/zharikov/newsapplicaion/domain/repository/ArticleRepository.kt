package by.zharikov.newsapplicaion.domain.repository

import by.zharikov.newsapplicaion.domain.model.Article
import kotlinx.coroutines.flow.Flow

interface ArticleRepository {

    fun getAllArticlesFromDb(): Flow<List<Article>>

    suspend fun deleteArticleFromDb(title: String)

    suspend fun insertArticleInDb(article: Article)

    suspend fun deleteAllArticlesFromDb()

    suspend fun insertAllArticles(articles: List<Article>)
}