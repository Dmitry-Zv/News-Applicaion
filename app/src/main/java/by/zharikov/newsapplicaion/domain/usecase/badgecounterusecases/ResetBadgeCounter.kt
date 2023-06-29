package by.zharikov.newsapplicaion.domain.usecase.badgecounterusecases

import android.content.SharedPreferences
import javax.inject.Inject

class ResetBadgeCounter @Inject constructor(private val sharedPreferences: SharedPreferences) {

    operator fun invoke(badgeCounterKey: String) {
        sharedPreferences.edit().remove(badgeCounterKey).apply()
    }
}