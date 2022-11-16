package by.zharikov.newsapplicaion.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.data.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RegistrationRepository(private val context: Context) {
    private val auth: FirebaseAuth = Firebase.auth
    private val database: DatabaseReference = Firebase.database.reference
    val currentUser = MutableLiveData<FirebaseUser>()
    val intent = MutableLiveData<Intent>()
    val deleteFlag = MutableLiveData<Boolean>()
    val user = MutableLiveData<User>()


    fun register(
        displayName: String,
        email: String,
        password: String
    ) {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
                currentUser.postValue(auth.currentUser)
                if (auth.currentUser != null) {
                    auth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
                        Toast.makeText(context, "Verification send", Toast.LENGTH_SHORT).show()
                        val user = User(displayName = displayName, email = email)
                        FirebaseAuth.getInstance()
                            .currentUser?.let {
                                Log.d("dBASE", database.child("user").child(it.uid).toString())
                                database.child("user")
                                    .child(it.uid)
                                    .setValue(user)
                                    .addOnCompleteListener {

                                        if (it.isSuccessful) Toast.makeText(
                                            context,
                                            "Add user: $displayName",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        else Toast.makeText(
                                            context,
                                            "Error: ${it.exception?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                            }
                    }?.addOnFailureListener {
                        Toast.makeText(
                            context,
                            "Email verification not sent",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

        }


    }


    fun login(
        email: String,
        password: String,
        view: View,
        login: (() -> Unit)
    ) {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()

                if (auth.currentUser != null) {
                    if (auth.currentUser?.isEmailVerified == true) {
                        login()
                    } else {
                        withContext(Dispatchers.Main) {
                            Snackbar.make(
                                view,
                                "The email isn't verified!",
                                Snackbar.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                } else {

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error: current user is null", Toast.LENGTH_SHORT)
                            .show()
                    }


                }
                Log.d("CurrenUser", currentUser.toString())
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                Log.d("CurrenUser", "Not")

            }
        }

    }

    fun signOut() {
        auth.signOut()
    }

    fun deleteUser(
        credential: AuthCredential
    ) {
        deleteFlag.value = false
        auth.currentUser?.reauthenticate(credential)?.addOnCompleteListener { result ->
            if (result.isSuccessful) {
                FirebaseAuth.getInstance().currentUser?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        database.child("user").child(it.uid).removeValue().await()
                        Log.d("dBASE", database.child("user").child(it.uid).toString())
                    }
                }
                auth.currentUser?.delete()?.addOnCompleteListener { resultD ->
                    if (resultD.isSuccessful) {

                        Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT)
                            .show()
                        deleteFlag.value = true
                        Log.d("DELETED_FLAG", deleteFlag.toString())
                    } else Toast.makeText(
                        context,
                        "Error: ${resultD.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else Toast.makeText(
                context,
                "Error: ${result.exception?.message}",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    fun changeEmail(credential: AuthCredential, email: String) {
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
                                if (taskVerified.isSuccessful) Toast.makeText(
                                    context,
                                    "Verification send",
                                    Toast.LENGTH_SHORT
                                ).show()
                                else Toast.makeText(
                                    context,
                                    "Error: ${taskVerified.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else Toast.makeText(
                        context,
                        "Error: ${taskChange.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }


    fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Reset link is sent to your email!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Reset link is not sent to your email! + ${result.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }


    }

    fun ifUserLogIn(ifLogin: (() -> Unit)) {
        auth.addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                if (auth.currentUser?.isEmailVerified == true) {
                    ifLogin()
                }

            }
        }
    }

    fun getUser() {
        auth.currentUser?.uid
            ?.let { it ->
                database.child("user").child(it).get().addOnSuccessListener { data ->
                    user.value = data.getValue<User>()!!
                    //  Log.d("USER_INFO", user.displayName.toString())
                }.addOnFailureListener {
                    Log.d("FIREBASE_ERROR", "${it.message}")
                }
            }
    }


    fun signInWithGoogle() {
        val option = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.server_client_id))
            .requestEmail()
            .build()
        val signInClient = GoogleSignIn.getClient(context, option)
        signInClient.signInIntent.also {
            intent.value = it
        }
    }

    fun googleAuthForFirebase(account: GoogleSignInAccount, goToMainActivity: (() -> Unit)) {
        val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.signInWithCredential(credentials).await()
                if (auth.currentUser != null) {
                    FirebaseAuth.getInstance().currentUser?.let {
                        val user = User(
                            displayName = account.displayName,
                            email = account.email.toString()
                        )
                        database.child("user")
                            .child(it.uid)
                            .setValue(user)
                            .addOnCompleteListener {

                                if (it.isSuccessful) Toast.makeText(
                                    context,
                                    "Add user: ${user.displayName}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                else Toast.makeText(
                                    context,
                                    "Error: ${it.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Successfully logged in", Toast.LENGTH_SHORT)
                            .show()
                        goToMainActivity()
                    }
                }
            } catch (e: ApiException) {
                withContext(Dispatchers.Main) {

                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}