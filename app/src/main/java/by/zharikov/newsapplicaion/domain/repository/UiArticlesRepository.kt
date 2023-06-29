package by.zharikov.newsapplicaion.domain.repository

import by.zharikov.newsapplicaion.domain.model.Article
import by.zharikov.newsapplicaion.utils.Resource

interface UiArticlesRepository {

    suspend fun saveUiArticleToFirebase(article: Article): Resource<Unit>

    suspend fun getUiArticlesFromFirebase(): Resource<List<Article>>

    suspend fun deleteUiArticleFromFirebase(publishedAt: String): Resource<Unit>
}