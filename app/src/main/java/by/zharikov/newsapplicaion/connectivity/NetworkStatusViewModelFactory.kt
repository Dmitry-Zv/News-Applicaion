package by.zharikov.newsapplicaion.connectivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class NetworkStatusViewModelFactory(private val networkStatusTracker: NetworkStatusTracker) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(NetworkStatusViewModel::class.java)) {
            NetworkStatusViewModel(this.networkStatusTracker) as T
        } else {
            throw IllegalArgumentException("ViewModel No Found")
        }
    }
}