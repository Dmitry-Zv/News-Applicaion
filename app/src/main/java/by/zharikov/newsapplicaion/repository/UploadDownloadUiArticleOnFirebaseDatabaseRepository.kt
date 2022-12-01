package by.zharikov.newsapplicaion.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import by.zharikov.newsapplicaion.data.model.Article
import by.zharikov.newsapplicaion.data.model.EntityArticle
import by.zharikov.newsapplicaion.data.model.UiArticle
import by.zharikov.newsapplicaion.db.ArticleDatabase
import by.zharikov.newsapplicaion.utils.EntityArticleToArticle
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class UploadDownloadUiArticleOnFirebaseDatabaseRepository(private val context: Context) {

    private val auth = Firebase.auth
    private val database = Firebase.database.reference
    private val pref: SharedPreferences by lazy {
        context.getSharedPreferences("ARTICLE_PREF_BOOL", Context.MODE_PRIVATE)
    }
    private val entityArticleToArticle = EntityArticleToArticle()
    val uiArticleListFromFirebase = MutableLiveData<List<UiArticle>>()


    suspend fun saveDataToFirebase() {

        val entityArticles = ArticleDatabase.geDatabase(context).getArticleDao().getAllArticles()
        val articles = mapFromEntityToArticle(entityArticles)
        val uiArticles = map(articles as MutableList<Article>)
        Log.d("ADD_TO_FDB", uiArticles.toString())
        auth.currentUser?.let {
            database.child("Article")
                .child(it.uid)
                .setValue(uiArticles)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(context, "Add to Firebase database", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(
                            context,
                            "Error: ${it.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        }
    }


    fun downloadUiArticleFromFirebaseDatabase() {
        auth.currentUser?.uid?.let {
            database.child("Article").child(it).get().addOnSuccessListener { data ->
                if (data.exists()) {
                    uiArticleListFromFirebase.value = data.getValue<List<UiArticle>>()!!
                    Log.d("UI_ARTICLES", uiArticleListFromFirebase.value.toString())
                }
            }.addOnFailureListener {
                Log.d("FIREBASE_ERROR", "${it.message}")
            }
        }
    }


    private fun map(articles: MutableList<Article>): MutableList<UiArticle> {
        val uiList = mutableListOf<UiArticle>()
        val isLiked = false
        for (article in articles) {
            uiList.add(UiArticle(article, pref.getBoolean(article.title, isLiked)))
        }
        return uiList
    }

    private fun mapFromEntityToArticle(entityArticles: List<EntityArticle>): List<Article> {
        val articles = arrayListOf<Article>()
        for (entity in entityArticles) {
            articles.add(entityArticleToArticle.map(entity))
        }
        return articles
    }

}