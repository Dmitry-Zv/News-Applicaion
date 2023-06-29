package by.zharikov.newsapplicaion.data.repository

import android.content.Context
import android.content.SharedPreferences
import by.zharikov.newsapplicaion.domain.repository.SharedArticlesRepository
import by.zharikov.newsapplicaion.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SharedArticleRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) :
    SharedArticlesRepository {
    override fun getPreferences(): SharedPreferences =
        context.getSharedPreferences(Constants.SHARED_PREFERENCES_ARTICLES, Context.MODE_PRIVATE)

}