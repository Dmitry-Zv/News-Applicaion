package by.zharikov.newsapplicaion.ui.authentication

import android.content.Intent
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import by.zharikov.newsapplicaion.repository.RegistrationRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

class LoginViewModel(private val registerRepository: RegistrationRepository) : ViewModel() {
    private val _intent = registerRepository.intent
    val intent: LiveData<Intent>
        get() = _intent


    fun login(email: String, password: String, view: View, login: (() -> Unit)) {
        registerRepository.login(email, password, view, login)
    }

    fun resetPassword(email: String) {
        registerRepository.resetPassword(email)
    }

    fun ifUserLogged(ifLogin: (() -> Unit)) {
        registerRepository.ifUserLogIn(ifLogin)
    }

    fun signInWithGoogle() {
        registerRepository.signInWithGoogle()
    }

    fun googleAuthForFirebaseVM(account: GoogleSignInAccount, goToMainActivity: (() -> Unit)) {
        registerRepository.googleAuthForFirebase(account, goToMainActivity)
    }
}