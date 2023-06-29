package by.zharikov.newsapplicaion.di

import by.zharikov.newsapplicaion.data.repository.*
import by.zharikov.newsapplicaion.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindArticleEntityRepository_toArticleEntityRepositoryImpl(articleRepositoryImpl: ArticleRepositoryImpl): ArticleRepository

    @Binds
    @Singleton
    fun bindNewsRepository_toNewsRepositoryImpl(newsRepositoryImpl: NewsRepositoryImpl): NewsRepository


    @Binds
    @Singleton
    fun bindFirebaseRepository_toFirebaseRepositoryImpl(firebaseRepositoryImpl: FirebaseRepositoryImpl): FirebaseRepository

    @Binds
    @Singleton
    fun bindImageRepository_toImageRepositoryImpl(imageRepositoryImpl: ImageRepositoryImpl): ImageRepository

    @Binds
    @Singleton
    fun bindTagRepository_toTagRepositoryImpl(tagRepositoryImpl: TagRepositoryImpl): TagRepository

    @Binds
    @Singleton
    fun bindUiArticlesRepository_toUiArticlesRepositoryImpl(uiArticlesRepositoryImpl: UiArticlesRepositoryImpl): UiArticlesRepository

    @Binds
    @Singleton
    fun bindSharedArticleRepository_toSharedArticleRepositoryImpl(sharedArticleRepositoryImpl: SharedArticleRepositoryImpl): SharedArticlesRepository
}