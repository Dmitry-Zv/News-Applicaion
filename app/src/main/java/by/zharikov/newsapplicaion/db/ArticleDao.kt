package by.zharikov.newsapplicaion.db

import androidx.room.*
import by.zharikov.newsapplicaion.data.model.EntityArticle

@Dao
interface ArticleDao {
    @Query("SELECT * FROM article")
    suspend fun getAllArticles(): List<EntityArticle>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: EntityArticle)

    @Query("DELETE FROM article WHERE title = :title")
    suspend fun deleteArticle(title: String)

    @Query("DELETE FROM article")
    suspend fun deleteAllArticle()

}