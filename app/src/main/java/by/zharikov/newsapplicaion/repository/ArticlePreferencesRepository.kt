package by.zharikov.newsapplicaion.repository

import android.content.SharedPreferences
import by.zharikov.newsapplicaion.domain.ArticleDataSource

class ArticlePreferencesRepository(private val sharedPreferences: SharedPreferences) :
    ArticleDataSource {
    override fun getArticleIsLiked(articleUrl: String?): Boolean {
        return sharedPreferences.getBoolean(articleUrl, false)
    }

    override fun setArticleIsLiked(articleUrl: String?) {
        sharedPreferences.edit().putBoolean(articleUrl, true).apply()
    }

    override fun deleteAllLikedArticles() {
        sharedPreferences.edit().clear().apply()
    }

    override fun deleteLikedArticle(articleUrl: String?) {
        sharedPreferences.edit().remove(articleUrl).apply()
    }
}