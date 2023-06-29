package by.zharikov.newsapplicaion.data.remote

import by.zharikov.newsapplicaion.domain.model.NewsModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {

    @GET("/v2/everything")
    suspend fun getEverything(
        @Query("q") query: String,
        @Query("page") page: Int,
    ): Response<NewsModel>

    @GET("/v2/top-headlines")
    suspend fun getTopHeadLines(
        @Query("country") country: String,
        @Query("page") page: Int,
    ): Response<NewsModel>

    @GET("/v2/top-headlines")
    suspend fun getTopHeadLinesCategory(
        @Query("country") country: String,
        @Query("category") category: String,
    ): Response<NewsModel>
}