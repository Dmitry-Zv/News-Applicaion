package by.zharikov.newsapplicaion.domain.usecase.newsusecases

import by.zharikov.newsapplicaion.domain.model.Article
import by.zharikov.newsapplicaion.domain.repository.NewsRepository
import by.zharikov.newsapplicaion.utils.Resource
import javax.inject.Inject

class GetTopHeadLinesCategory @Inject constructor(private val repository: NewsRepository) {

    suspend operator fun invoke(country: String, category: String): Resource<List<Article>> {
        return try {
            val response = repository.newGetTopHeadLinesCategory(
                country = country,
                category = category
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    return@let Resource.Success(data = it.articles)
                } ?: Resource.Success(data = emptyList())
            } else {
                Resource.Error(msg = "Error: Bad response.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(msg = e.message ?: "Unknown error")
        }
    }
}