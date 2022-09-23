package by.zharikov.newsapplicaion.api

import by.zharikov.newsapplicaion.data.model.NewsModel
import by.zharikov.newsapplicaion.utils.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {

    @GET("/v2/everything")
    suspend fun getEverything(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("apiKey") apiKey: String = Constants.API_KEY
    ) : Response<NewsModel>

    @GET("/v2/top-headlines")
    suspend fun getTopHeadLines(
        @Query("country") country: String,
        @Query("page") page: Int,
        @Query("apiKey") apiKey: String = Constants.API_KEY
    ) : Response<NewsModel>
}