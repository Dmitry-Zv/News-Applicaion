package by.zharikov.newsapplicaion.di

import by.zharikov.newsapplicaion.BuildConfig
import by.zharikov.newsapplicaion.data.remote.NewsApi
import by.zharikov.newsapplicaion.domain.repository.NewsRepository
import by.zharikov.newsapplicaion.domain.usecase.newsusecases.GetEverythingArticles
import by.zharikov.newsapplicaion.domain.usecase.newsusecases.GetTopHeadLinesArticle
import by.zharikov.newsapplicaion.domain.usecase.newsusecases.GetTopHeadLinesCategory
import by.zharikov.newsapplicaion.domain.usecase.newsusecases.NewsUseCases
import by.zharikov.newsapplicaion.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {

    @Provides
    @Singleton
    fun provideHttpLoginInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

    @Provides
    @Singleton
    fun provideClient(loginInterceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor { chain ->
                        val request = chain.request()
                        val newRequest =
                            request.newBuilder().header("Authorization", Constants.API_KEY)
                        chain.proceed(newRequest.build())
                    }
                    addInterceptor(loginInterceptor)
                    pingInterval(1, TimeUnit.SECONDS)
                }
            }
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideNewsApi(retrofit: Retrofit): NewsApi =
        retrofit.create(NewsApi::class.java)

    @Provides
    @Singleton
    fun provideNewsUseCases(
        repository: NewsRepository
    ): NewsUseCases =
        NewsUseCases(
            getEverythingArticles = GetEverythingArticles(repository),
            getTopHeadLinesArticle = GetTopHeadLinesArticle(repository),
            getTopHeadLinesCategory = GetTopHeadLinesCategory(repository)
        )


}