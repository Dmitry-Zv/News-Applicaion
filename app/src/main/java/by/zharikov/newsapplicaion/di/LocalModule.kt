package by.zharikov.newsapplicaion.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import by.zharikov.newsapplicaion.data.local.ArticleDao
import by.zharikov.newsapplicaion.data.local.ArticleDatabase
import by.zharikov.newsapplicaion.domain.repository.ArticleRepository
import by.zharikov.newsapplicaion.domain.repository.SharedArticlesRepository
import by.zharikov.newsapplicaion.domain.usecase.badgecounterusecases.BadgeCounterUseCases
import by.zharikov.newsapplicaion.domain.usecase.badgecounterusecases.GetBadgeCounter
import by.zharikov.newsapplicaion.domain.usecase.badgecounterusecases.ResetBadgeCounter
import by.zharikov.newsapplicaion.domain.usecase.badgecounterusecases.SetBadgeCounter
import by.zharikov.newsapplicaion.domain.usecase.entityarticleusecases.*
import by.zharikov.newsapplicaion.utils.Constants.Companion.ARTICLE_DATABASE
import by.zharikov.newsapplicaion.utils.Constants.Companion.SHARED_PREFERENCES
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,

        ): ArticleDatabase =
        Room.databaseBuilder(context, ArticleDatabase::class.java, ARTICLE_DATABASE)
            .build()

    @Provides
    @Singleton
    fun provideArticleDao(database: ArticleDatabase): ArticleDao =
        database.getArticleDao()

    @Provides
    @Singleton
    fun provideEntityArticleUseCases(repository: ArticleRepository, sharedArticlesRepository: SharedArticlesRepository): EntityArticleUseCases =
        EntityArticleUseCases(
            deleteAllArticlesFromDb = DeleteAllArticlesFromDb(repository, sharedArticlesRepository),
            deleteArticleFromDb = DeleteArticleFromDb(repository, sharedArticlesRepository),
            insertArticleInDb = InsertArticleInDb(repository, sharedArticlesRepository),
            getAllArticlesFromDb = GetAllArticlesFromDb(repository),
            insertAllArticlesInDb = InsertAllArticlesInDb(repository, sharedArticlesRepository),
            getArticleSaveState = GetArticleSaveState(sharedArticlesRepository)
        )

    @Provides
    @Singleton
    fun provideBadgeCounterUseCases(sharedPreferences: SharedPreferences):BadgeCounterUseCases =
        BadgeCounterUseCases(
            getBadgeCounter = GetBadgeCounter(sharedPreferences),
            setBadgeCounter = SetBadgeCounter(sharedPreferences),
            resetBadgeCounter = ResetBadgeCounter(sharedPreferences)
        )


    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE)



}