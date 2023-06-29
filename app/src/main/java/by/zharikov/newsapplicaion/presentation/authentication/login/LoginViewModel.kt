package by.zharikov.newsapplicaion.presentation.authentication.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.zharikov.newsapplicaion.domain.usecase.entityarticleusecases.EntityArticleUseCases
import by.zharikov.newsapplicaion.domain.usecase.firebaseusecases.FirebaseUseCases
import by.zharikov.newsapplicaion.domain.usecase.uiarticlesusecases.UiArticlesUseCases
import by.zharikov.newsapplicaion.presentation.common.Event
import by.zharikov.newsapplicaion.utils.Resource
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseUseCases: FirebaseUseCases,
    private val uiArticlesUseCases: UiArticlesUseCases,
    private val entityArticleUseCases: EntityArticleUseCases
) : ViewModel(), Event<LoginEvent> {

    private val _state = MutableStateFlow(LoginState.default)
    val state = _state.asStateFlow()


    override fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnLogin -> login(email = event.email, password = event.password)
            is LoginEvent.OnLoginWithGoogle -> loginWithGoogle(account = event.account)
            is LoginEvent.OnResetPassword -> resetPassword(email = event.email)
            LoginEvent.OnSignUp -> signUpScreen()
            LoginEvent.Default -> preformDefault()
        }
    }

    private fun preformDefault() {
        _state.value = LoginState.default
    }

    private fun signUpScreen() {
        _state.value = _state.value.copy(data = LoginStateName.SUCCESS_PRESS_SIGN_UP.name)
    }

    private fun resetPassword(email: String) {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            when (val result = firebaseUseCases.resetPassword(email = email)) {
                is Resource.Error -> _state.value =
                    _state.value.copy(data = "", error = result.msg, isLoading = false)
                is Resource.Success -> _state.value = _state.value.copy(
                    data = LoginStateName.SUCCESS_RESTORE_PASSWORD.name,
                    error = null,
                    isLoading = false
                )
            }
        }
    }

    private fun login(email: String, password: String) {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            when (val result = firebaseUseCases.login(email = email, password = password)) {
                is Resource.Error -> _state.value =
                    _state.value.copy(data = "", error = result.msg, isLoading = false)
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        data = LoginStateName.SUCCESS_LOGIN.name,
                        error = null,
                        isLoading = false
                    )
                    saveArticlesFromFirebase()
                }
            }

        }
    }

    private fun saveArticlesFromFirebase() {
        viewModelScope.launch {
            when (val result = uiArticlesUseCases.getUiArticlesFromFirebase()) {

                is Resource.Error -> _state.value = _state.value.copy(error = result.msg)
                is Resource.Success -> {
                    entityArticleUseCases.insertAllArticlesInDb(articles = result.data)
                }
            }
        }
    }

    private fun loginWithGoogle(account: GoogleSignInAccount) {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            when (val result = firebaseUseCases.googleAuth(account = account)) {
                is Resource.Error -> _state.value =
                    _state.value.copy(data = "", error = result.msg, isLoading = false)
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        data = LoginStateName.SUCCESS_LOGIN_WITH_GOOGLE.name,
                        error = null,
                        isLoading = false
                    )
                    saveArticlesFromFirebase()
                }
            }
        }
    }


}