package by.zharikov.newsapplicaion.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.zharikov.newsapplicaion.repository.FirebaseRepository
import by.zharikov.newsapplicaion.repository.ResultFirebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


sealed class UiStateSplash {
    object UserLoginStateSuccess : UiStateSplash()
    object UserLoginStateUnSuccess : UiStateSplash()
    object Initial : UiStateSplash()
}

class SplashViewModel(private val firebaseRepository: FirebaseRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiStateSplash>(UiStateSplash.Initial)
    val uiState = _uiState.asStateFlow()

    init {
        ifUserLogIn()
        viewModelScope.launch {
            firebaseRepository.resultState.collectLatest { result ->
                when (result) {
                    is ResultFirebase.IfUserLoginTrue -> _uiState.value =
                        UiStateSplash.UserLoginStateSuccess
                    is ResultFirebase.IfUserLoginFalse -> _uiState.value =
                        UiStateSplash.UserLoginStateUnSuccess
                    else -> {}
                }
            }
        }
    }


    private fun ifUserLogIn() {
        viewModelScope.launch {
            firebaseRepository.ifUserLogIn()
        }
    }

}