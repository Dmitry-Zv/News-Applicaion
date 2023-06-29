package by.zharikov.newsapplicaion.data.repository

import android.content.SharedPreferences
import by.zharikov.newsapplicaion.domain.model.TagModelUi
import by.zharikov.newsapplicaion.domain.model.TagModels
import by.zharikov.newsapplicaion.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(private val sharedPref: SharedPreferences) :
    TagRepository {


    override suspend fun setTags(tagModelUi: TagModelUi) {
        val tagList = TagModels.tagModelList
        tagList.map { tag ->
            if (tag.tagName == tagModelUi.tagModel.tagName) {
                TagModelUi(tag, tagModelUi.isClicked)
                sharedPref.edit().putBoolean(tag.tagName, tagModelUi.isClicked).apply()
            } else {
                TagModelUi(tag, false)
                sharedPref.edit().putBoolean(tag.tagName, false).apply()
            }
        }

    }


    override suspend fun getTags(): Flow<List<TagModelUi>> {
        val tagList = TagModels.tagModelList
        val tagUiList = tagList.map { tag ->
            TagModelUi(tag, sharedPref.getBoolean(tag.tagName, false))
        }
        return flow { emit(tagUiList) }
    }

}