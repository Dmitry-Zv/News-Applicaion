package by.zharikov.newsapplicaion.domain.usecase.imageusecases

import android.net.Uri
import by.zharikov.newsapplicaion.domain.repository.ImageRepository
import by.zharikov.newsapplicaion.utils.Resource
import javax.inject.Inject

class SetImage @Inject constructor(private val repository: ImageRepository) {

    suspend operator fun invoke(data: Uri): Resource<Unit> {

        if (data.path == null) return Resource.Error(msg = "No path...")
        return repository.setImage(data = data)
    }
}