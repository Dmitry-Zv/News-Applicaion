package by.zharikov.newsapplicaion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.zharikov.newsapplicaion.repository.FirebaseRepository
import java.lang.IllegalArgumentException

class MainViewModelActivityFactory(private val firebaseRepository: FirebaseRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            MainViewModel(this.firebaseRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel No Found")
        }
    }
}