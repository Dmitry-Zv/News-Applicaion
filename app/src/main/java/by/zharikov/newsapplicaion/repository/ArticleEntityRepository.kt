package by.zharikov.newsapplicaion.repository

import android.content.Context
import by.zharikov.newsapplicaion.data.model.EntityArticle
import by.zharikov.newsapplicaion.db.ArticleDatabase

class ArticleEntityRepository(private val context: Context) {

    suspend fun repGetAllArticles(): List<EntityArticle> =
        ArticleDatabase.geDatabase(context).getArticleDao().getAllArticles()

    suspend fun repDeleteArticle(title: String) {
        ArticleDatabase.geDatabase(context).getArticleDao().deleteArticle(title = title)
    }

    suspend fun repInsertArticle(article: EntityArticle) {
        ArticleDatabase.geDatabase(context).getArticleDao().insertArticle(article = article)
    }

    suspend fun repDeleteAllArticle() {
        ArticleDatabase.geDatabase(context).getArticleDao().deleteAllArticle()
    }

}