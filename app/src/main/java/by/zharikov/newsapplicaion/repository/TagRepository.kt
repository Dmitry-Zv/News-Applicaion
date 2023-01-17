package by.zharikov.newsapplicaion.repository

import android.content.Context
import android.content.SharedPreferences
import by.zharikov.newsapplicaion.data.model.TagModelUi
import by.zharikov.newsapplicaion.data.model.TagModels

class TagRepository(val context: Context) {
    private val sharedPref: SharedPreferences by lazy {
        context.getSharedPreferences("TAGS_LIST", Context.MODE_PRIVATE)
    }

    fun setTags(tagUiModel: TagModelUi): MutableList<TagModelUi> {
        val tagList = TagModels.tagModelList
        val tagUiList = mutableListOf<TagModelUi>()
        if (tagUiModel.isClicked) {
            tagList.forEach { tag ->
                if (tag.tagName == tagUiModel.tagModel.tagName) {
                    tagUiList.add(TagModelUi(tag, true))
                    sharedPref.edit().putBoolean(tag.tagName, true).apply()
                } else {
                    tagUiList.add(TagModelUi(tag, false))
                    sharedPref.edit().putBoolean(tag.tagName, false).apply()
                }

            }
        } else tagList.forEach { tag ->
            tagUiList.add(TagModelUi(tag, false))
            sharedPref.edit().putBoolean(tag.tagName, false).apply()
        }
        return tagUiList
    }


    fun getTagsBySharedPreferences(): MutableList<TagModelUi> {
        val tagList = TagModels.tagModelList
        val tagUiList = mutableListOf<TagModelUi>()
        tagList.forEach { tag ->
            tagUiList.add(TagModelUi(tag, sharedPref.getBoolean(tag.tagName, false)))
        }
        return tagUiList
    }

}