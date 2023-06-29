package by.zharikov.newsapplicaion.di

import by.zharikov.newsapplicaion.domain.repository.TagRepository
import by.zharikov.newsapplicaion.domain.usecase.tagusecases.GetTags
import by.zharikov.newsapplicaion.domain.usecase.tagusecases.SetTags
import by.zharikov.newsapplicaion.domain.usecase.tagusecases.TagUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TagModule {

    @Provides
    @Singleton
    fun provideTagUseCases(repository: TagRepository): TagUseCases =
        TagUseCases(
            getTags = GetTags(repository),
            setTags = SetTags(repository)
        )
}