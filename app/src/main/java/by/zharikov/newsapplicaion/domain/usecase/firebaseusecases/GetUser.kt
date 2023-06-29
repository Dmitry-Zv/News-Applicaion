package by.zharikov.newsapplicaion.domain.usecase.firebaseusecases

import by.zharikov.newsapplicaion.domain.model.User
import by.zharikov.newsapplicaion.domain.repository.FirebaseRepository
import by.zharikov.newsapplicaion.utils.Resource
import javax.inject.Inject

class GetUser @Inject constructor(private val repository: FirebaseRepository) {

    suspend operator fun invoke(): Resource<User> {
        return repository.getUser()
    }
}