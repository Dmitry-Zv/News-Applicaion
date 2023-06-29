package by.zharikov.newsapplicaion.domain.usecase.newsusecases

import by.zharikov.newsapplicaion.domain.model.Article
import by.zharikov.newsapplicaion.domain.repository.NewsRepository
import by.zharikov.newsapplicaion.utils.Resource
import javax.inject.Inject

class GetTopHeadLinesArticle @Inject constructor(private val repository: NewsRepository) {

    suspend operator fun invoke(country: String, pageNumber: Int): Resource<List<Article>> {
        return try {
            val response =
                repository.newsGetTopHeadlines(country = country, pageNumber = pageNumber)

            if (response.isSuccessful) {
                response.body()?.let {
                    return@let Resource.Success(data = it.articles)
                } ?: Resource.Success(data = emptyList())
            } else {
                Resource.Error(msg = "Error: Bad response.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Unknown error")
        }
    }
}