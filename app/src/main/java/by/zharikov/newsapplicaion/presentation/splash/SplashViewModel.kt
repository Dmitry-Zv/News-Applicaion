package by.zharikov.newsapplicaion.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.zharikov.newsapplicaion.domain.usecase.firebaseusecases.FirebaseUseCases
import by.zharikov.newsapplicaion.presentation.common.Event
import by.zharikov.newsapplicaion.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SplashViewModel @Inject constructor(private val firebaseUseCases: FirebaseUseCases) :
    ViewModel(), Event<SplashEvent> {

    private val _state = MutableStateFlow(SplashState.default)
    val state = _state.asStateFlow()

    override fun onEvent(event: SplashEvent) {
        when (event) {
            SplashEvent.CheckUserLogin -> checkUserLogin()
            SplashEvent.Default -> performDefault()
        }
    }

    private fun performDefault() {
        _state.value = SplashState.default
    }

    private fun checkUserLogin() {
        viewModelScope.launch {
            when (firebaseUseCases.ifUserLogin()) {
                is Resource.Error -> _state.value =
                    _state.value.copy(state = SplashStateName.ERROR.name)
                is Resource.Success -> _state.value =
                    _state.value.copy(state = SplashStateName.SUCCESS.name)
            }
        }
    }


}