package by.zharikov.newsapplicaion.presentation.authentication.register

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.domain.usecase.firebaseusecases.FirebaseUseCases
import by.zharikov.newsapplicaion.domain.usecase.imageusecases.ImagesUseCases
import by.zharikov.newsapplicaion.presentation.common.Event
import by.zharikov.newsapplicaion.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val firebaseUseCases: FirebaseUseCases,
    private val imagesUseCases: ImagesUseCases
) :
    ViewModel(), Event<RegisterEvent> {

    private val _state = MutableStateFlow(RegisterState.default)
    val state = _state.asStateFlow()

    override fun onEvent(event: RegisterEvent) {
        when (event) {
            RegisterEvent.OnAlreadyHaveAnAccount -> pressOnAlreadyHaveAnAccount()
            is RegisterEvent.OnRegister -> register(
                email = event.email,
                password = event.password,
                repeatPassword = event.repeatPassword,
                displayName = event.displayName
            )
            RegisterEvent.Default -> performDefault()
        }
    }

    private fun register(
        email: String,
        password: String,
        repeatPassword: String,
        displayName: String
    ) {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            when (val result = firebaseUseCases.register(
                email = email,
                password = password,
                repeatPassword = repeatPassword,
                displayName = displayName
            )) {
                is Resource.Error -> _state.value =
                    _state.value.copy(data = "", error = result.msg, isLoading = false)
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        data = RegisterStateName.REGISTER.name,
                        error = null,
                        isLoading = false
                    )
                    imagesUseCases.setImage(Uri.parse("android.resource://by.zharikov.newsapplicaion/${R.drawable.profile_image}"))
                }
            }
        }
    }


    private fun performDefault() {
        _state.value = RegisterState.default
    }

    private fun pressOnAlreadyHaveAnAccount() {
        _state.value = _state.value.copy(data = RegisterStateName.ALREADY_HAVE_AN_ACCOUNT.name)

    }


}