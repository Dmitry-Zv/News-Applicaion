package by.zharikov.newsapplicaion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.zharikov.newsapplicaion.data.model.User
import by.zharikov.newsapplicaion.repository.FirebaseRepository
import by.zharikov.newsapplicaion.repository.ResultFirebase
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class UiStateAccount {
    object Initial : UiStateAccount()
    object DeleteAccount : UiStateAccount()
    object SignOut : UiStateAccount()
    object ChangeEmail : UiStateAccount()
    class Error(val exception: Exception) : UiStateAccount()
    class GetUser(val user: User?) : UiStateAccount()
}

class MainViewModel(private val firebaseRepository: FirebaseRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<UiStateAccount>(UiStateAccount.Initial)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getUser()
            firebaseRepository.resultState.collectLatest { resultState ->
                when (resultState) {
                    is ResultFirebase.SignOut -> _uiState.value = UiStateAccount.SignOut
                    is ResultFirebase.DeleteUser -> _uiState.value = UiStateAccount.DeleteAccount
                    is ResultFirebase.ChangeEmail -> _uiState.value = UiStateAccount.ChangeEmail
                    is ResultFirebase.GetUser -> _uiState.value =
                        UiStateAccount.GetUser(user = resultState.user)
                    is ResultFirebase.Error -> _uiState.value =
                        UiStateAccount.Error(exception = resultState.exception)
                    else -> {}
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            firebaseRepository.signOut()

        }
    }


    fun deleteAccount(credential: AuthCredential) {
        viewModelScope.launch {
            firebaseRepository.deleteUser(credential = credential)
        }
    }

    fun changeAccount(credential: AuthCredential, email: String, profileName: String) {
        viewModelScope.launch {
            firebaseRepository.changeEmail(
                credential = credential,
                email = email,
                profileName = profileName
            )
        }
    }

    fun getUser() {
        viewModelScope.launch {
            firebaseRepository.getUser()
        }
    }


}