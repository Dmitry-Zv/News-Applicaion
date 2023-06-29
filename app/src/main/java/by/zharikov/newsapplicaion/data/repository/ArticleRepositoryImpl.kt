package by.zharikov.newsapplicaion.data.repository

import by.zharikov.newsapplicaion.data.local.ArticleDao
import by.zharikov.newsapplicaion.domain.model.Article
import by.zharikov.newsapplicaion.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ArticleRepositoryImpl @Inject constructor(private val articleDao: ArticleDao) :
    ArticleRepository {
    override fun getAllArticlesFromDb(): Flow<List<Article>> =
        articleDao.getAllArticles()


    override suspend fun deleteArticleFromDb(title: String) {
        articleDao.deleteArticle(title = title)
    }

    override suspend fun insertArticleInDb(article: Article) {
        articleDao.insertArticle(article = article)
    }

    override suspend fun deleteAllArticlesFromDb() {
        articleDao.deleteAllArticle()
    }

    override suspend fun insertAllArticles(articles: List<Article>) {
        articleDao.insertAllArticles(articles = articles)
    }


}