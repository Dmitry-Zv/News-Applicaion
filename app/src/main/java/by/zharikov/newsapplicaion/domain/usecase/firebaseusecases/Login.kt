package by.zharikov.newsapplicaion.domain.usecase.firebaseusecases

import android.content.SharedPreferences
import android.util.Patterns
import by.zharikov.newsapplicaion.domain.repository.FirebaseRepository
import by.zharikov.newsapplicaion.utils.Constants
import by.zharikov.newsapplicaion.utils.Resource
import javax.inject.Inject

class Login @Inject constructor(
    private val repository: FirebaseRepository,
    private val sharedPreferences: SharedPreferences
) {

    suspend operator fun invoke(email: String, password: String): Resource<Unit> {
        sharedPreferences.edit().putString(Constants.SIGN_IN_METHOD, Constants.EMAIL_PASSWORD)
            .apply()
        if (email.isBlank()) return Resource.Error(msg = "Email is blank")
        if (!Patterns.EMAIL_ADDRESS.matcher(email)
                .matches()
        ) return Resource.Error(msg = "Email address doesn't match form ")
        if (password.isBlank()) return Resource.Error(msg = "Password is blank")

        return repository.login(email = email, password = password)
    }
}