package by.zharikov.newsapplicaion.presentation

import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

sealed class MainActivityEvent {
    object Default : MainActivityEvent()
    object SignOut : MainActivityEvent()
    data class ChangeEmail(
        val email: String,
        val password: String,
        val newEmail: String,
        val displayName: String
    ) : MainActivityEvent()

    data class ShowError(val msg: String) : MainActivityEvent()
    data class SetImage(val image: Uri) : MainActivityEvent()
    data class DeleteUser(
        val email: String? = null,
        val password: String? = null
    ) :
        MainActivityEvent()

    object GetUser : MainActivityEvent()
    object GetImage : MainActivityEvent()
    object OnSignInMethod : MainActivityEvent()
}