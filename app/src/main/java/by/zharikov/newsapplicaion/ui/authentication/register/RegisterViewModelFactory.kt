package by.zharikov.newsapplicaion.ui.authentication.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.zharikov.newsapplicaion.usecase.registration_firebase_use_case.RegistrationFirebaseUseCase
import java.lang.IllegalArgumentException

class RegisterViewModelFactory(private val registerFirebaseUseCase: RegistrationFirebaseUseCase) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            RegisterViewModel(
                this.registerFirebaseUseCase
            ) as T
        } else {
            throw IllegalArgumentException("ViewModel No Found")
        }
    }
}