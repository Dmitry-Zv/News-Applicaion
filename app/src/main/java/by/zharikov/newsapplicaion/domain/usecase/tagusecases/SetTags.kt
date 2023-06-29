package by.zharikov.newsapplicaion.domain.usecase.tagusecases

import by.zharikov.newsapplicaion.domain.model.TagModelUi
import by.zharikov.newsapplicaion.domain.repository.TagRepository
import javax.inject.Inject

class SetTags @Inject constructor(private val repository: TagRepository) {

    suspend operator fun invoke(tagModelUi: TagModelUi) {

        repository.setTags(tagModelUi = tagModelUi)
    }
}