package by.zharikov.newsapplicaion.utils

import by.zharikov.newsapplicaion.data.model.Article
import by.zharikov.newsapplicaion.data.model.EntityArticle

class ArticleToEntityArticle {
    fun map(article: Article): EntityArticle {
        return EntityArticle(
            author = article.author,
            title = article.title,
            content = article.content,
            description = article.description,
            publishedAt = article.publishedAt,
            url = article.url,
            urlToImage = article.urlToImage,
            source = article.source
        )
    }
}