package by.zharikov.newsapplicaion.domain.usecase.firebaseusecases

import android.content.SharedPreferences
import by.zharikov.newsapplicaion.domain.repository.FirebaseRepository
import by.zharikov.newsapplicaion.utils.Constants
import by.zharikov.newsapplicaion.utils.Resource
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import javax.inject.Inject

class GoogleAuth @Inject constructor(
    private val repository: FirebaseRepository,
    private val sharedPreferences: SharedPreferences
) {

    suspend operator fun invoke(account: GoogleSignInAccount): Resource<Unit> {
        sharedPreferences.edit().putString(Constants.SIGN_IN_METHOD, Constants.GOOGLE).apply()
        return repository.googleAuthForFirebase(account = account)
    }
}