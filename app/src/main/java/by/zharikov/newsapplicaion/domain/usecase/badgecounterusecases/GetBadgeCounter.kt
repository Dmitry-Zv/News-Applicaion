package by.zharikov.newsapplicaion.domain.usecase.badgecounterusecases

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

class GetBadgeCounter @Inject constructor(private val sharedPreferences: SharedPreferences) {

    operator fun invoke(badgeCounterKey:String): List<String> {
        val json = sharedPreferences.getString(badgeCounterKey, "")
        json?.let {
            if (it.isBlank()) return emptyList()
            else return Gson().fromJson(it, object : TypeToken<List<String>>() {}.type)

        } ?: return emptyList()
    }
}