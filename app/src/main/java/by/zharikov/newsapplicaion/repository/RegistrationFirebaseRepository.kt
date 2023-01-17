package by.zharikov.newsapplicaion.repository

import android.content.Context
import android.util.Log
import android.widget.Toast
import by.zharikov.newsapplicaion.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class RegistrationFirebaseRepository(private val context: Context) {
    private val auth: FirebaseAuth = Firebase.auth
    private val database: DatabaseReference = Firebase.database.reference

    suspend fun register(
        displayName: String,
        email: String,
        password: String
    ): FirebaseUser? {


        auth.createUserWithEmailAndPassword(email, password).await()
        if (auth.currentUser != null) {
            auth.currentUser?.sendEmailVerification()?.addOnSuccessListener {
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
        return auth.currentUser


    }


}