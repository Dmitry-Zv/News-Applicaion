package by.zharikov.newsapplicaion.di

import by.zharikov.newsapplicaion.domain.repository.UiArticlesRepository
import by.zharikov.newsapplicaion.domain.usecase.uiarticlesusecases.DeleteUiArticleFromFirebase
import by.zharikov.newsapplicaion.domain.usecase.uiarticlesusecases.GetUiArticlesFromFirebase
import by.zharikov.newsapplicaion.domain.usecase.uiarticlesusecases.SaveUiArticlesToFirebase
import by.zharikov.newsapplicaion.domain.usecase.uiarticlesusecases.UiArticlesUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UiArticlesModule {

    @Provides
    @Singleton
    fun provideUiArticlesUseCases(repository: UiArticlesRepository): UiArticlesUseCases =
        UiArticlesUseCases(
            getUiArticlesFromFirebase = GetUiArticlesFromFirebase(repository),
            saveUiArticlesToFirebase = SaveUiArticlesToFirebase(repository),
            deleteUiArticleFromFirebase = DeleteUiArticleFromFirebase(repository)
        )
}