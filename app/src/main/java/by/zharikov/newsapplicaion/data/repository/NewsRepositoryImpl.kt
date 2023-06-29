package by.zharikov.newsapplicaion.data.repository

import by.zharikov.newsapplicaion.data.remote.NewsApi
import by.zharikov.newsapplicaion.domain.model.NewsModel
import by.zharikov.newsapplicaion.domain.repository.NewsRepository
import retrofit2.Response
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(private val api: NewsApi) : NewsRepository {
    override suspend fun newsGetTopHeadlines(
        country: String,
        pageNumber: Int
    ): Response<NewsModel> =
        api.getTopHeadLines(country = country, page = pageNumber)

    override suspend fun newsGetEverything(q: String, pageNumber: Int): Response<NewsModel> =
        api.getEverything(query = q, page = pageNumber)

    override suspend fun newGetTopHeadLinesCategory(
        country: String,
        category: String
    ): Response<NewsModel> =
        api.getTopHeadLinesCategory(
            country = country,
            category = category
        )
}