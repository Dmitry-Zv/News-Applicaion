package by.zharikov.newsapplicaion.domain.usecase.badgecounterusecases

import android.content.SharedPreferences
import com.google.gson.Gson
import javax.inject.Inject

class SetBadgeCounter @Inject constructor(private val sharedPreferences: SharedPreferences) {

    operator fun invoke(listOfArticlesTitle: List<String>, badgeCounterKey: String) {
        val json = Gson().toJson(listOfArticlesTitle)
        sharedPreferences.edit().putString(badgeCounterKey, json).apply()
    }
}