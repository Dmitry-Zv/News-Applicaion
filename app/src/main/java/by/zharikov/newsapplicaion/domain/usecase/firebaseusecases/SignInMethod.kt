package by.zharikov.newsapplicaion.domain.usecase.firebaseusecases

import android.content.SharedPreferences
import by.zharikov.newsapplicaion.utils.Constants
import by.zharikov.newsapplicaion.utils.Resource
import javax.inject.Inject

class SignInMethod @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    operator fun invoke(): Resource<String> =
        sharedPreferences.getString(Constants.SIGN_IN_METHOD, "")?.let {
            Resource.Success(it)
        } ?: Resource.Error("String is null...")

}