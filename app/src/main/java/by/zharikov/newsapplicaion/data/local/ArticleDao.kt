package by.zharikov.newsapplicaion.data.local

import androidx.room.*
import by.zharikov.newsapplicaion.domain.model.Article
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM article")
    fun getAllArticles(): Flow<List<Article>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: Article)

    @Query("DELETE FROM article WHERE title = :title")
    suspend fun deleteArticle(title: String)

    @Query("DELETE FROM article")
    suspend fun deleteAllArticle()

    @Insert
    suspend fun insertAllArticles(articles: List<Article>)

}