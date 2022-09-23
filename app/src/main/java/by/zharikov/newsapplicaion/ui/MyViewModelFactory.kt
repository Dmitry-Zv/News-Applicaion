package by.zharikov.newsapplicaion.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import by.zharikov.newsapplicaion.repository.NewsRepository
import by.zharikov.newsapplicaion.ui.main.MainFragmentViewModel
import java.lang.IllegalArgumentException

class MyViewModelFactory(private val newsRepository: NewsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MainFragmentViewModel::class.java)){
            MainFragmentViewModel(this.newsRepository) as T
        } else {
            throw IllegalArgumentException("ViewModel No Found")
        }
    }
}