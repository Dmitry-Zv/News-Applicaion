package by.zharikov.newsapplicaion.utils

import by.zharikov.newsapplicaion.data.model.Article
import by.zharikov.newsapplicaion.data.model.EntityArticle
import by.zharikov.newsapplicaion.data.model.Source

class EntityArticleToArticle {
    fun map(entityArticle: EntityArticle): Article {
        return Article(
            title = entityArticle.title,
            description = entityArticle.description,
            url = entityArticle.url,
            urlToImage = entityArticle.urlToImage,
            publishedAt = entityArticle.publishedAt,
            content = entityArticle.content,
            author = entityArticle.author,
            source = entityArticle.source

        )
    }
}