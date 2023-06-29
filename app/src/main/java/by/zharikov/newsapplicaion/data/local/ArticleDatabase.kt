package by.zharikov.newsapplicaion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import by.zharikov.newsapplicaion.domain.model.Article

@Database(entities = [Article::class], version = 3)
abstract class ArticleDatabase : RoomDatabase() {
    abstract fun getArticleDao(): ArticleDao

}