package by.zharikov.newsapplicaion.ui.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.zharikov.newsapplicaion.repository.RegistrationRepository
import java.lang.IllegalArgumentException

class LoginViewModelFactory(private val registerRepository: RegistrationRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            LoginViewModel(this.registerRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel No Found")
        }
    }
}