package by.zharikov.newsapplicaion.usecase

import android.util.Log
import by.zharikov.newsapplicaion.data.model.Article
import by.zharikov.newsapplicaion.data.model.UiArticle
import by.zharikov.newsapplicaion.repository.ArticleEntityRepository
import by.zharikov.newsapplicaion.repository.ArticlePreferencesRepository
import by.zharikov.newsapplicaion.utils.ArticleToEntityArticle
import by.zharikov.newsapplicaion.utils.EntityArticleToArticle


sealed class Result {
    object EmptyArticles : Result()
    class UiArticles(val uiArticles: MutableList<UiArticle>) : Result()
    class Success(val success: String) : Result()
    class Error(val exception: Exception) : Result()
}

class ArticlePreferencesUseCase(
    private val articlePreferencesRepository: ArticlePreferencesRepository,
    private val articleEntityRepository: ArticleEntityRepository
) {

    suspend fun invokeGetAllArticles(): Result {
        val uiArticles = mutableListOf<UiArticle>()
        val entityArticleToArticle = EntityArticleToArticle()
        return try {
            if (articleEntityRepository.repGetAllArticles().isNotEmpty()) {
                articleEntityRepository.repGetAllArticles().forEach {
                    uiArticles.add(
                        UiArticle(
                            entityArticleToArticle.map(it),
                            articlePreferencesRepository.getArticleIsLiked(it.url)
                        )
                    )
                }
                Result.UiArticles(uiArticles)
            } else {
                Log.d("EMPTY_LIST", "EMPTY LIST")
                Result.EmptyArticles
            }
        } catch (e: Exception) {
            Result.Error(e)
        }

    }

    suspend fun invokeSetSavedArticle(article: Article): Result {
        return try {
            articleEntityRepository.repInsertArticle(articleToEntityArticle.map(article))
            articlePreferencesRepository.setArticleIsLiked(article.url)

            Result.Success("Article Saved")
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun invokeDeleteSavedArticle(article: Article): Result {
        return try {
            article.title?.let { articleEntityRepository.repDeleteArticle(it) }
            articlePreferencesRepository.deleteLikedArticle(article.url)
            Result.Success("Article Deleted")

        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun invokeDeleteAllSavedArticle(): Result {
        return try {
            articleEntityRepository.repDeleteAllArticle()
            articlePreferencesRepository.deleteAllLikedArticles()
            return Result.EmptyArticles
        } catch (e: Exception) {
            Result.Error(e)
        }

    }

    companion object {
        private val articleToEntityArticle = ArticleToEntityArticle()
    }

}