package by.zharikov.newsapplicaion.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.zharikov.newsapplicaion.repository.FirebaseRepository
import java.lang.IllegalArgumentException

class SplashViewModelFactory(private val firebaseRepository: FirebaseRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            SplashViewModel(this.firebaseRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel No Found")
        }
    }
}