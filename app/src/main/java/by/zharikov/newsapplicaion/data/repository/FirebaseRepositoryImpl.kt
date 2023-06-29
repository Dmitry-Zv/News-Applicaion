package by.zharikov.newsapplicaion.data.repository

import by.zharikov.newsapplicaion.domain.model.User
import by.zharikov.newsapplicaion.domain.repository.FirebaseRepository
import by.zharikov.newsapplicaion.utils.Resource
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val databaseReference: DatabaseReference,
    private val lastSignInAccount: GoogleSignInAccount?,
    private val storageRef: StorageReference
) : FirebaseRepository {


    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    currentUser.sendEmailVerification().await()
                    val user = User(displayName = displayName, email = email)
                    databaseReference.child("user").child(currentUser.uid)
                        .setValue(user).await()
                    Resource.Success(data = Unit)
                } else {
                    Resource.Error(msg = "Current user is null!")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Unknown error...")
            }
        }
    }


    override suspend fun login(
        email: String,
        password: String,
    ): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {

                val authResult: AuthResult =
                    auth.signInWithEmailAndPassword(email, password).await()
                authResult.user?.let {
                    if (it.isEmailVerified) {
                        Resource.Success(data = Unit)
                    } else {
                        Resource.Error(msg = "Email isn't verified...")
                    }
                } ?: Resource.Error(msg = "User is null...")
            } catch (e: Exception) {
                Resource.Error(msg = e.message ?: "Unknown error!")
            }
        }


    }

    override suspend fun googleAuthForFirebase(account: GoogleSignInAccount): Resource<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credentials).await()
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val user = User(
                        displayName = account.displayName,
                        email = account.email ?: "No email"
                    )
                    databaseReference.child("user").child(currentUser.uid)
                        .setValue(user)
                    Resource.Success(data = Unit)

                } else {
                    Resource.Error(msg = "Current user is null")
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Unknown error...")
            }
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun deleteUser(
        email: String?,
        password: String?
    ): Resource<Unit> {

        return withContext(Dispatchers.IO) {
            try {
                val authCredential = if (email != null && password != null) {
                    EmailAuthProvider.getCredential(email, password)
                } else {

                    GoogleAuthProvider.getCredential(lastSignInAccount?.idToken, null)
                }
                auth.currentUser?.reauthenticate(authCredential)?.await()

                auth.currentUser?.let { user ->
                    databaseReference.child("user").child(user.uid).removeValue().await()
                    databaseReference.child("Article").child(user.uid).removeValue().await()
                    val profileImageRef = storageRef.child("images/${user.uid}/profile.jpg")
                    profileImageRef.delete().await()
                    user.delete().await()
                    return@let Resource.Success(data = Unit)

                } ?: Resource.Error(msg = "User is null!")
            } catch (e: Exception) {
                Resource.Error(msg = e.message ?: "Unknown error...")
            }
        }
    }

    override suspend fun changeEmail(
        authCredential: AuthCredential,
        email: String,
        profileName: String
    ): Resource<Unit> {

        return withContext(Dispatchers.IO) {
            try {
                auth.currentUser?.let {
                    it.reauthenticate(authCredential).await()
                    it.updateEmail(email).await()
                    databaseReference.child("user")
                        .child(it.uid)
                        .setValue(
                            User(
                                displayName = profileName,
                                email = email
                            )
                        )
                    it.sendEmailVerification().await()
                    return@let Resource.Success(data = Unit)

                } ?: Resource.Error(msg = "User is null")
            } catch (e: Exception) {
                Resource.Error(msg = e.message ?: "Unknown error")
            }
        }

    }


    override suspend fun resetPassword(email: String): Resource<Unit> {

        return withContext(Dispatchers.IO) {

            try {
                auth.sendPasswordResetEmail(email).await()
                Resource.Success(data = Unit)
            } catch (e: Exception) {
                Resource.Error(msg = e.message ?: "Unknown error...")
            }

        }
    }

    override suspend fun ifUserLogin(): Resource<Unit> {
        return if (auth.currentUser != null) {
            Resource.Success(data = Unit)

        } else Resource.Error(msg = "User is null!")

    }

    override suspend fun getUser(): Resource<User> {
        return withContext(Dispatchers.IO) {
            try {
                auth.currentUser?.let { user ->
                    val snapshot = databaseReference.child("user").child(user.uid).get().await()
                    val data = snapshot.getValue<User>()
                    return@let if (data != null) Resource.Success(data = data)
                    else Resource.Error(msg = "Data is null")
                } ?: Resource.Error("User is null!")
            } catch (e: Exception) {
                Resource.Error(msg = e.message ?: "Unknown error...")
            }
        }

    }


}


