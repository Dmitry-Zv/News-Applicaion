package by.zharikov.newsapplicaion.domain.usecase.firebaseusecases

import android.util.Patterns
import by.zharikov.newsapplicaion.domain.repository.FirebaseRepository
import by.zharikov.newsapplicaion.utils.Resource
import javax.inject.Inject

class Register @Inject constructor(private val repository: FirebaseRepository) {

    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String,
        repeatPassword: String
    ): Resource<Unit> {

        if (email.isBlank()) return Resource.Error(msg = "Email is blank")
        if (!Patterns.EMAIL_ADDRESS.matcher(email)
                .matches()
        ) return Resource.Error(msg = "Email address doesn't match form")
        if (password.isBlank()) return Resource.Error(msg = "Password is blank")
        if (displayName.isBlank()) return Resource.Error(msg = "Display name is blank")
        if (password != repeatPassword) return Resource.Error(msg = "Repeated password doesn't match password")
        return repository.register(email = email, password = password, displayName = displayName)
    }
}