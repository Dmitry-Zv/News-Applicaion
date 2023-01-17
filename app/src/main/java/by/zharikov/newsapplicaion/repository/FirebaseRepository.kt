package by.zharikov.newsapplicaion.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.data.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


sealed class ResultFirebase {
    object Initial : ResultFirebase()
    class Login(val currentUser: FirebaseUser) : ResultFirebase()
    object SingInWithGoogle : ResultFirebase()
    object SignOut : ResultFirebase()
    object IfUserLoginTrue : ResultFirebase()
    object IfUserLoginFalse : ResultFirebase()
    class Error(val exception: Exception) : ResultFirebase()
    object ResetPassword : ResultFirebase()
    object ChangeEmail : ResultFirebase()
    object DeleteUser : ResultFirebase()
    class GetUser(val user: User?) : ResultFirebase()


}

class FirebaseRepository(private val context: Context) {
    private val auth: FirebaseAuth = Firebase.auth
    private val database: DatabaseReference = Firebase.database.reference
    private val _intent = MutableSharedFlow<Intent>()
    val intent = _intent.asSharedFlow()
    private val _resultState = MutableStateFlow<ResultFirebase>(ResultFirebase.Initial)
    val resultState = _resultState.asStateFlow()


    suspend fun login(
        email: String,
        password: String,

        ) {

        auth.signInWithEmailAndPassword(email, password).await()

        if (auth.currentUser != null) {
            _resultState.value = ResultFirebase.Login(auth.currentUser!!)

        } else {

            _resultState.value = ResultFirebase.Error(Exception("User in null!"))
        }


    }

    fun signOut() {
        auth.signOut()
        _resultState.value = ResultFirebase.SignOut

    }

    fun deleteUser(
        credential: AuthCredential
    ) {
        auth.currentUser?.reauthenticate(credential)?.addOnCompleteListener { result ->
            if (result.isSuccessful) {
                FirebaseAuth.getInstance().currentUser?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        database.child("user").child(it.uid).removeValue().await()
                        database.child("Article").child(it.uid).removeValue().await()
                        Log.d("dBASE", database.child("user").child(it.uid).toString())
                    }
                }
                auth.currentUser?.delete()?.addOnCompleteListener { resultD ->
                    if (resultD.isSuccessful)
                        _resultState.value = ResultFirebase.DeleteUser
                    else _resultState.value = ResultFirebase.Error(exception = resultD.exception!!)

                }
            } else _resultState.value = ResultFirebase.Error(exception = result.exception!!)
        }

    }

    fun changeEmail(
        credential: AuthCredential,
        email: String,
        profileName: String
    ) {
        auth.currentUser?.reauthenticate(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                auth.currentUser?.updateEmail(email)?.addOnCompleteListener { taskChange ->
                    if (taskChange.isSuccessful) {
                        Toast.makeText(
                            context,
                            "Email is updated",
                            Toast.LENGTH_SHORT
                        ).show()
                        auth.currentUser?.sendEmailVerification()
                            ?.addOnCompleteListener { taskVerified ->
                                if (taskVerified.isSuccessful) {
                                    auth.currentUser?.let {
                                        database.child("user").child(it.uid).setValue(
                                            User(profileName, email)
                                        )
                                    }
                                    _resultState.value = ResultFirebase.ChangeEmail
                                } else _resultState.value =
                                    ResultFirebase.Error(exception = taskVerified.exception!!)
                            }
                    } else _resultState.value =
                        ResultFirebase.Error(exception = taskChange.exception!!)
                }
            } else _resultState.value = ResultFirebase.Error(exception = task.exception!!)
        }
    }


    fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { result ->
                if (result.isSuccessful) _resultState.value = ResultFirebase.ResetPassword
                else _resultState.value = ResultFirebase.Error(exception = result.exception!!)
            }
    }

    fun ifUserLogIn() {
        auth.addAuthStateListener { auth ->
            if (auth.currentUser != null) _resultState.value = ResultFirebase.IfUserLoginTrue
            else _resultState.value = ResultFirebase.IfUserLoginFalse
        }
    }

    fun getUser() {
        auth.currentUser?.uid
            ?.let { it ->
                database.child("user").child(it).get().addOnSuccessListener { data ->
                    if (data.exists()) _resultState.value =
                        ResultFirebase.GetUser(data.getValue<User>())

                    //  Log.d("USER_INFO", user.displayName.toString())
                }.addOnFailureListener {
                    _resultState.value = ResultFirebase.Error(exception = it)
                }
            }
    }


    fun signInWithGoogle() {
        CoroutineScope(Dispatchers.IO).launch {
            val option = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.server_client_id))
                .requestEmail()
                .build()
            val signInClient = GoogleSignIn.getClient(context, option)
            signInClient.signInIntent.also {
                _intent.emit(it)
            }
        }
    }

    suspend fun googleAuthForFirebase(account: GoogleSignInAccount) {
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)

        auth.signInWithCredential(credentials).await()
        if (auth.currentUser != null) {
            FirebaseAuth.getInstance().currentUser?.let {
                val user = User(
                    displayName = account.displayName,
                    email = account.email.toString()
                )
                database.child("user").child(it.uid).get()
                    .addOnSuccessListener { dataSnapshot ->
                        if (!dataSnapshot.exists()) {
                            database.child("user")
                                .child(it.uid)
                                .setValue(user)
                                .addOnCompleteListener {

                                    if (it.isSuccessful) _resultState.value =
                                        ResultFirebase.SingInWithGoogle
                                    else _resultState.value =
                                        ResultFirebase.Error(exception = it.exception!!)
                                }
                        } else {
                            _resultState.value =
                                ResultFirebase.SingInWithGoogle
                        }
                    }

            }
        }


    }
}


