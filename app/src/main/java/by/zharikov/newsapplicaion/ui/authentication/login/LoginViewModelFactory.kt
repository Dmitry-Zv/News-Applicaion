package by.zharikov.newsapplicaion.ui.authentication.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.zharikov.newsapplicaion.repository.FirebaseRepository
import java.lang.IllegalArgumentException

class LoginViewModelFactory(private val firebaseRepository: FirebaseRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            LoginViewModel(
                this.firebaseRepository
            ) as T
        } else {
            throw IllegalArgumentException("ViewModel No Found")
        }
    }
}
