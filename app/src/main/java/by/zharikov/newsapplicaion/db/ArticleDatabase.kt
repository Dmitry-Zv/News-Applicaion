package by.zharikov.newsapplicaion.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import by.zharikov.newsapplicaion.data.model.EntityArticle

@Database(entities = [EntityArticle::class], version = 3)
abstract class ArticleDatabase : RoomDatabase() {
    abstract fun getArticleDao(): ArticleDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE 'source' ('source_id' TEXT, 'name' TEXT NOT NULL)")
            }

        }

        private val MIGRATION_2_3 = object : Migration(2, 3){
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE article ADD COLUMN source_id TEXT")
                database.execSQL("ALTER TABLE article ADD COLUMN name TEXT ")
            }

        }

        private var INSTANCE: ArticleDatabase? = null
        fun geDatabase(context: Context): ArticleDatabase {
            if (INSTANCE == null) {
                synchronized(this) {
                    INSTANCE = Room.databaseBuilder(
                        context,
                        ArticleDatabase::class.java,
                        "article_database"
                    )
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                        .build()
                }
            }
            return INSTANCE!!
        }
    }



}