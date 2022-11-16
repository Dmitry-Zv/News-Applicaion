package by.zharikov.newsapplicaion.ui.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.zharikov.newsapplicaion.repository.RegistrationRepository
import java.lang.IllegalArgumentException

class RegistrationViewModelFactory(private val registrationRepository: RegistrationRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(RegistrationViewModel::class.java)) {
            RegistrationViewModel(this.registrationRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel No Found")
        }
    }
}