package by.zharikov.newsapplicaion.domain.repository

import android.content.SharedPreferences

interface SharedArticlesRepository {

    fun getPreferences():SharedPreferences
}