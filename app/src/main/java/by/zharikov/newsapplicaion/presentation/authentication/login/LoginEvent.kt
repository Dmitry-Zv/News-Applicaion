package by.zharikov.newsapplicaion.presentation.authentication.login

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

sealed class LoginEvent() {
    data class OnLogin(val email: String, val password: String) : LoginEvent()
    data class OnLoginWithGoogle(val account: GoogleSignInAccount) : LoginEvent()
    data class OnResetPassword(val email: String) : LoginEvent()
    object OnSignUp : LoginEvent()
    object Default : LoginEvent()

}
