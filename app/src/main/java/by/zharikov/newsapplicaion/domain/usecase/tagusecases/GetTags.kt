package by.zharikov.newsapplicaion.domain.usecase.tagusecases

import by.zharikov.newsapplicaion.domain.model.TagModelUi
import by.zharikov.newsapplicaion.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTags @Inject constructor(private val repository: TagRepository) {

    suspend operator fun invoke(): Flow<List<TagModelUi>> =
        repository.getTags()

}