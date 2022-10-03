package by.zharikov.newsapplicaion.repository

import by.zharikov.newsapplicaion.api.RetrofitNews

class NewsRepository(private val retrofitNews: RetrofitNews) {
    suspend fun newsGetTopHeadlines(country: String, pageNumber: Int) =
        retrofitNews.getApi().getTopHeadLines(country, pageNumber)

    suspend fun newsGetEverything(q: String, pageNumber: Int) =
        retrofitNews.getApi().getEverything(q, pageNumber)

}