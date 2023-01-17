package by.zharikov.newsapplicaion.usecase.registration_firebase_use_case

import by.zharikov.newsapplicaion.repository.RegistrationFirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class ResultRegister {
    object SuccessRegistration : ResultRegister()
    class Error(val exception: Exception) : ResultRegister()
    object Initial : ResultRegister()
}

class RegistrationFirebaseUseCase(private val registrationFirebaseRepository: RegistrationFirebaseRepository) {

    private val _resultState = MutableStateFlow<ResultRegister>(ResultRegister.Initial)
    val resultState = _resultState.asStateFlow()

    suspend fun invokeRegisterAuth(displayName: String, email: String, password: String) {
        try {
            val currentUser = registrationFirebaseRepository.register(
                displayName = displayName,
                email = email,
                password = password
            )
            if (currentUser != null) _resultState.value = ResultRegister.SuccessRegistration
            else throw Exception("Current user is null!")
        } catch (exception: Exception) {
            _resultState.value = ResultRegister.Error(exception = exception)
        }

    }

}