package by.zharikov.newsapplicaion.domain.repository

import by.zharikov.newsapplicaion.domain.model.TagModelUi
import kotlinx.coroutines.flow.Flow

interface TagRepository {

    suspend fun setTags(tagModelUi: TagModelUi)

    suspend fun getTags(): Flow<List<TagModelUi>>
}