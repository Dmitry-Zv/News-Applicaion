package by.zharikov.newsapplicaion.domain.usecase.entityarticleusecases

import by.zharikov.newsapplicaion.domain.model.Article
import by.zharikov.newsapplicaion.domain.repository.ArticleRepository
import by.zharikov.newsapplicaion.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllArticlesFromDb @Inject constructor(private val repository: ArticleRepository) {

    operator fun invoke(): Resource<Flow<List<Article>>> {
        return try {
            val article = repository.getAllArticlesFromDb()
            Resource.Success(data = article)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(msg = e.message ?: "Unknown error")
        }
    }
}
