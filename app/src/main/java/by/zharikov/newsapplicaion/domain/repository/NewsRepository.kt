package by.zharikov.newsapplicaion.domain.repository

import by.zharikov.newsapplicaion.domain.model.NewsModel
import retrofit2.Response

interface NewsRepository {

    suspend fun newsGetTopHeadlines(country: String, pageNumber: Int): Response<NewsModel>

    suspend fun newsGetEverything(q: String, pageNumber: Int): Response<NewsModel>

    suspend fun newGetTopHeadLinesCategory(country: String, category: String): Response<NewsModel>
}