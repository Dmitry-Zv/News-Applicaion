package by.zharikov.newsapplicaion.data.repository

import by.zharikov.newsapplicaion.domain.model.Article
import by.zharikov.newsapplicaion.domain.repository.UiArticlesRepository
import by.zharikov.newsapplicaion.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UiArticlesRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val databaseReference: DatabaseReference
) : UiArticlesRepository {


    override suspend fun saveUiArticleToFirebase(article: Article): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {

                auth.currentUser?.let {
                    databaseReference.child("Article")
                        .child(it.uid)
                        .child(article.publishedAt ?: "Default time...")
                        .setValue(article)
                        .await()
                    Resource.Success(data = Unit)
                } ?: Resource.Error(msg = "Current user is null.")
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Unknown error...")
            }
        }
    }


    override suspend fun getUiArticlesFromFirebase(): Resource<List<Article>> {

        return withContext(Dispatchers.IO) {
            try {
                auth.currentUser?.uid?.let {
                    val snapshot = databaseReference.child("Article").child(it).get().await()
                    val mapArticles = snapshot.getValue<Map<String, Article>>()
                    mapArticles?.let { mapOfArticles ->
                        val articles = mapOfArticles.map { article ->
                            article.value
                        }
                        Resource.Success(data = articles)
                    } ?: Resource.Error(
                        msg = "Articles is null"
                    )
                } ?: Resource.Error(msg = "Current user is null")
            } catch (e: Exception) {
                Resource.Error(msg = e.message ?: "Unknown error...")
            }
        }

    }

    override suspend fun deleteUiArticleFromFirebase(publishedAt: String): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                auth.currentUser?.let {
                    databaseReference.child("Article")
                        .child(it.uid)
                        .child(publishedAt)
                        .removeValue()
                        .await()
                    Resource.Success(data = Unit)
                } ?: Resource.Error(msg = "Current user is null")
            } catch (e: Exception) {
                Resource.Error(msg = e.message ?: "Unknown error...")
            }
        }
    }


}