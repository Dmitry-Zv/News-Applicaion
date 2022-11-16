package by.zharikov.newsapplicaion.ui.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import by.zharikov.newsapplicaion.repository.RegistrationRepository
import com.google.firebase.auth.FirebaseUser

class RegistrationViewModel(private val registrationRepository: RegistrationRepository) :
    ViewModel() {

    private val _currentUser = registrationRepository.currentUser
    val currentUser: LiveData<FirebaseUser>
        get() = _currentUser


    fun register(
        email: String,
        userName: String,
        password: String,
    ) {
        registrationRepository.register(displayName = userName, email = email, password = password)
    }


}