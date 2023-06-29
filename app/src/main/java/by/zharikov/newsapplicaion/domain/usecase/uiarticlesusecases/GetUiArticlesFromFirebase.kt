package by.zharikov.newsapplicaion.domain.usecase.uiarticlesusecases

import by.zharikov.newsapplicaion.domain.model.Article
import by.zharikov.newsapplicaion.domain.repository.UiArticlesRepository
import by.zharikov.newsapplicaion.utils.Resource
import javax.inject.Inject

class GetUiArticlesFromFirebase @Inject constructor(private val repository: UiArticlesRepository) {

    suspend operator fun invoke(): Resource<List<Article>> {
        return repository.getUiArticlesFromFirebase()
    }

}