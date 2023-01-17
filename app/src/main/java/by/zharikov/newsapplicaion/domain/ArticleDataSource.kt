package by.zharikov.newsapplicaion.domain

interface ArticleDataSource {

    fun getArticleIsLiked(articleUrl: String?): Boolean

    fun setArticleIsLiked(articleUrl: String?)

    fun deleteAllLikedArticles()

    fun deleteLikedArticle(articleUrl: String?)

}