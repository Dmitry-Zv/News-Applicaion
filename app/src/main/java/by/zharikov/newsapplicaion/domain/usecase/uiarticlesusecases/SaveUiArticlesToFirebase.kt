package by.zharikov.newsapplicaion.domain.usecase.uiarticlesusecases

import by.zharikov.newsapplicaion.domain.model.Article
import by.zharikov.newsapplicaion.domain.repository.UiArticlesRepository
import by.zharikov.newsapplicaion.utils.Resource
import javax.inject.Inject

class SaveUiArticlesToFirebase @Inject constructor(private val repository: UiArticlesRepository) {

    suspend operator fun invoke(article: Article): Resource<Unit> {

        return repository.saveUiArticleToFirebase(article = article)
    }
}