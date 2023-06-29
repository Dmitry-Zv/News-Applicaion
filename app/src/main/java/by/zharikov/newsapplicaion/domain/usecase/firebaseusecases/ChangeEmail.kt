package by.zharikov.newsapplicaion.domain.usecase.firebaseusecases

import by.zharikov.newsapplicaion.domain.repository.FirebaseRepository
import by.zharikov.newsapplicaion.utils.Resource
import com.google.firebase.auth.AuthCredential
import javax.inject.Inject

class ChangeEmail @Inject constructor(private val repository: FirebaseRepository) {

    suspend operator fun invoke(
        credential: AuthCredential,
        email: String,
        displayName: String
    ): Resource<Unit> {

        if (email.isBlank()) return Resource.Error(msg = "Email is blank")
        if (displayName.isBlank()) return Resource.Error(msg = "Display name is blank")
        return repository.changeEmail(
            authCredential = credential,
            email = email,
            profileName = displayName
        )
    }
}