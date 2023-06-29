package by.zharikov.newsapplicaion.domain.usecase.firebaseusecases

import by.zharikov.newsapplicaion.domain.repository.FirebaseRepository
import javax.inject.Inject

class SignOut @Inject constructor(private val repository: FirebaseRepository) {

    suspend operator fun invoke() {
        repository.signOut()
    }
}