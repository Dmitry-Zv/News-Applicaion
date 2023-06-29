package by.zharikov.newsapplicaion.domain.usecase.newsusecases

data class NewsUseCases(
    val getEverythingArticles: GetEverythingArticles,
    val getTopHeadLinesCategory: GetTopHeadLinesCategory,
    val getTopHeadLinesArticle: GetTopHeadLinesArticle
)
