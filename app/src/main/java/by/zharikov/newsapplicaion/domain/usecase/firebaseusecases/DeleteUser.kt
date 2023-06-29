package by.zharikov.newsapplicaion.domain.usecase.firebaseusecases

import android.content.SharedPreferences
import by.zharikov.newsapplicaion.domain.repository.FirebaseRepository
import by.zharikov.newsapplicaion.utils.Constants
import by.zharikov.newsapplicaion.utils.Resource
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import javax.inject.Inject

class DeleteUser @Inject constructor(
    private val repository: FirebaseRepository,
    private val sharedPreferences: SharedPreferences
) {

    suspend operator fun invoke(email: String?, password: String?): Resource<Unit> {
        sharedPreferences.edit().remove(Constants.SIGN_IN_METHOD).apply()
        return repository.deleteUser(email = email, password = password)
    }
}