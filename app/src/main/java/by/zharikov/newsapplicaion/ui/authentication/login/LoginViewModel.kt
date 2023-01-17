package by.zharikov.newsapplicaion.ui.authentication.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.zharikov.newsapplicaion.repository.FirebaseRepository
import by.zharikov.newsapplicaion.repository.ResultFirebase
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


sealed class UiStateFirebaseLogin {
    class Login(val currentUser: FirebaseUser) : UiStateFirebaseLogin()
    class Error(val exception: Exception) : UiStateFirebaseLogin()
    object LoginWithGoogle : UiStateFirebaseLogin()
    object Initial : UiStateFirebaseLogin()
    object ResetPassword : UiStateFirebaseLogin()
    object Load : UiStateFirebaseLogin()
}

class LoginViewModel(
    private val fireBaseRepository: FirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiStateFirebaseLogin>(UiStateFirebaseLogin.Initial)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            fireBaseRepository.resultState.collectLatest { resultState ->
                when (resultState) {
                    is ResultFirebase.Login -> _uiState.value =
                        UiStateFirebaseLogin.Login(resultState.currentUser)
                    is ResultFirebase.SingInWithGoogle -> _uiState.value =
                        UiStateFirebaseLogin.LoginWithGoogle
                    is ResultFirebase.ResetPassword -> _uiState.value =
                        UiStateFirebaseLogin.ResetPassword
                    is ResultFirebase.Error -> _uiState.value =
                        UiStateFirebaseLogin.Error(exception = resultState.exception)
                    else -> {}
                }

            }
        }

    }


    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiStateFirebaseLogin.Load
            try {
                fireBaseRepository.login(email = email, password = password)

            } catch (exception: Exception) {
                _uiState.value = UiStateFirebaseLogin.Error(exception = exception)
            }
        }
    }

    fun loginWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            try {
                fireBaseRepository.googleAuthForFirebase(account = account)

            } catch (exception: Exception) {
                _uiState.value = UiStateFirebaseLogin.Error(exception = exception)
            }
        }
    }


    fun resetPassword(email: String) {
        viewModelScope.launch {
            fireBaseRepository.resetPassword(email = email)
        }
    }


}