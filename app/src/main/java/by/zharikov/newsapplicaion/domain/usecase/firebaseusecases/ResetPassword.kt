package by.zharikov.newsapplicaion.domain.usecase.firebaseusecases

import by.zharikov.newsapplicaion.domain.repository.FirebaseRepository
import by.zharikov.newsapplicaion.utils.Resource
import javax.inject.Inject

class ResetPassword @Inject constructor(private val repository: FirebaseRepository) {

    suspend operator fun invoke(email: String): Resource<Unit> {
        if (email.isBlank()) return Resource.Error(msg = "Email is blank")
        return repository.resetPassword(email = email)
    }
}