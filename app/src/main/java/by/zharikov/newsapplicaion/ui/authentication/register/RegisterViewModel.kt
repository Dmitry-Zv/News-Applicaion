package by.zharikov.newsapplicaion.ui.authentication.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.zharikov.newsapplicaion.usecase.registration_firebase_use_case.RegistrationFirebaseUseCase
import by.zharikov.newsapplicaion.usecase.registration_firebase_use_case.ResultRegister
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class UiStateRegister() {
    object ShowNotification : UiStateRegister()
    class ShowError(val exception: Exception) : UiStateRegister()
    object Initial : UiStateRegister()
    object Loading : UiStateRegister()
}

class RegisterViewModel(private val registrationFirebaseUseCase: RegistrationFirebaseUseCase) :
    ViewModel() {

    private val _uiState = MutableStateFlow<UiStateRegister>(UiStateRegister.Initial)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            registrationFirebaseUseCase.resultState.collectLatest { result ->
                when (result) {
                    is ResultRegister.SuccessRegistration -> _uiState.value =
                        UiStateRegister.ShowNotification
                    is ResultRegister.Error -> _uiState.value =
                        UiStateRegister.ShowError(result.exception)
                    else -> {}
                }
            }
        }

    }

    fun registerAuth(displayName: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiStateRegister.Loading
            registrationFirebaseUseCase.invokeRegisterAuth(
                displayName = displayName,
                email = email,
                password = password
            )
        }

    }


}