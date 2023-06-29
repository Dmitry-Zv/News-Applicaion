package by.zharikov.newsapplicaion.domain.usecase.firebaseusecases

import by.zharikov.newsapplicaion.domain.repository.FirebaseRepository
import by.zharikov.newsapplicaion.utils.Resource
import javax.inject.Inject

class IfUserLogin @Inject constructor(private val repository: FirebaseRepository) {

    suspend operator fun invoke(): Resource<Unit> {
        return repository.ifUserLogin()
    }
}