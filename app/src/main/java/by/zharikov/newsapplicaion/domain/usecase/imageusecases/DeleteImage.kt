package by.zharikov.newsapplicaion.domain.usecase.imageusecases

import by.zharikov.newsapplicaion.domain.repository.ImageRepository
import by.zharikov.newsapplicaion.utils.Resource
import javax.inject.Inject

class DeleteImage @Inject constructor(private val repository: ImageRepository) {

    suspend operator fun invoke(): Resource<Unit> =
        repository.deleteImage()

}