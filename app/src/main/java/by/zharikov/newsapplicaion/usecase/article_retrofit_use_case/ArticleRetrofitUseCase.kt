package by.zharikov.newsapplicaion.usecase.article_retrofit_use_case

import by.zharikov.newsapplicaion.data.model.Article
import by.zharikov.newsapplicaion.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class Result {
    class SuccessTopHeadlinesArticles(val articles: List<Article>) : Result()
    class SuccessArticleGetEverything(val articles: List<Article>) : Result()
    class SuccessArticleGetTopHeadLinesCategory(val articles: List<Article>) : Result()
    class Error(val exception: Exception) : Result()
    object Initial : Result()
}

class ArticleRetrofitUseCase(private val newsRepository: NewsRepository) {

    private val _resultState = MutableStateFlow<Result>(Result.Initial)
    val resultState = _resultState.asStateFlow()

    suspend fun invokeGetEverythingArticle(q: String, pageNumber: Int) {
        try {
            val response = newsRepository.newsGetEverything(q = q, pageNumber = pageNumber)
            if (response.isSuccessful) _resultState.value =
                Result.SuccessArticleGetEverything(articles = response.body()!!.articles)
        } catch (exception: Exception) {
            _resultState.value = Result.Error(exception = exception)
        }
    }

    suspend fun invokeGetTopHeadLinesArticle(country: String, pageNumber: Int) {
        try {
            val response =
                newsRepository.newsGetTopHeadlines(country = country, pageNumber = pageNumber)
            if (response.isSuccessful) _resultState.value =
                Result.SuccessTopHeadlinesArticles(articles = response.body()!!.articles)
        } catch (exception: Exception) {
            _resultState.value = Result.Error(exception = exception)
        }
    }

    suspend fun invokeGetTopHeadLinesCategoryArticle(country: String, category: String) {
        try {
            val response =
                newsRepository.newGetTopHeadLinesCategory(country = country, category = category)
            if (response.isSuccessful) _resultState.value =
                Result.SuccessArticleGetTopHeadLinesCategory(articles = response.body()!!.articles)
        } catch (exception: Exception) {
            _resultState.value = Result.Error(exception = exception)
        }
    }

}