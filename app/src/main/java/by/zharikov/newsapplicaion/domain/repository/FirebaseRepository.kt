package by.zharikov.newsapplicaion.domain.repository

import by.zharikov.newsapplicaion.domain.model.User
import by.zharikov.newsapplicaion.utils.Resource
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.AuthCredential

interface FirebaseRepository {

    suspend fun register(email: String, password: String, displayName: String): Resource<Unit>

    suspend fun login(email: String, password: String): Resource<Unit>

    suspend fun googleAuthForFirebase(account: GoogleSignInAccount): Resource<Unit>

    suspend fun signOut()

    suspend fun deleteUser(
        email: String?, password: String?
    ): Resource<Unit>

    suspend fun changeEmail(
        authCredential: AuthCredential,
        email: String,
        profileName: String
    ): Resource<Unit>

    suspend fun resetPassword(email: String): Resource<Unit>

    suspend fun ifUserLogin(): Resource<Unit>

    suspend fun getUser(): Resource<User>


}