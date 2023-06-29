package by.zharikov.newsapplicaion.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.zharikov.newsapplicaion.domain.usecase.entityarticleusecases.EntityArticleUseCases
import by.zharikov.newsapplicaion.domain.usecase.firebaseusecases.FirebaseUseCases
import by.zharikov.newsapplicaion.domain.usecase.imageusecases.ImagesUseCases
import by.zharikov.newsapplicaion.presentation.common.Event
import by.zharikov.newsapplicaion.utils.Resource
import com.google.firebase.auth.EmailAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val firebaseUseCases: FirebaseUseCases,
    private val imagesUseCases: ImagesUseCases,
    private val entityArticleUseCases: EntityArticleUseCases
) : ViewModel(), Event<MainActivityEvent> {
    private val _state = MutableStateFlow<MainActivityState>(MainActivityState.Default)
    val state = _state.asStateFlow()


    init {
        performSignInMethod()
        getImage()
        getUser()
    }

    override fun onEvent(event: MainActivityEvent) {
        when (event) {
            is MainActivityEvent.ChangeEmail -> changeEmail(
                email = event.email,
                password = event.password,
                newEmail = event.newEmail,
                displayName = event.displayName
            )
            MainActivityEvent.Default -> performDefault()
            MainActivityEvent.GetImage -> getImage()
            MainActivityEvent.GetUser -> getUser()
            is MainActivityEvent.ShowError -> showError(msg = event.msg)
            MainActivityEvent.SignOut -> signOut()
            is MainActivityEvent.SetImage -> setImage(image = event.image)
            is MainActivityEvent.DeleteUser -> deleteUser(
                email = event.email,
                password = event.password
            )
            MainActivityEvent.OnSignInMethod -> performSignInMethod()
        }
    }

    private fun performSignInMethod() {
        viewModelScope.launch {
            when (val result = firebaseUseCases.signInMethod()) {
                is Resource.Error -> _state.value = MainActivityState.Error(msg = result.msg)
                is Resource.Success -> _state.value =
                    MainActivityState.SignInMethod(data = result.data)
            }
        }
    }

    private fun deleteUser(
        email: String?,
        password: String?
    ) {
        if (email != null && password != null) {
            if (email.isBlank() || password.isBlank()) {
                showError(msg = "Email or password is blank.")
                return
            }
        }
        viewModelScope.launch {
            when (val result =
                firebaseUseCases.deleteUser(email = email, password = password)) {
                is Resource.Error -> {
                    _state.value = MainActivityState.Error(msg = result.msg)
                }
                is Resource.Success -> {
                    entityArticleUseCases.deleteAllArticlesFromDb()
                }
            }
        }

    }

    private fun setImage(image: Uri) {
        viewModelScope.launch {
            when (val result = imagesUseCases.setImage(data = image)) {
                is Resource.Error -> _state.value = MainActivityState.Error(msg = result.msg)
                else -> {}
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            entityArticleUseCases.deleteAllArticlesFromDb()
            firebaseUseCases.signOut()
            _state.value = MainActivityState.SignOut
        }
    }

    private fun showError(msg: String) {
        _state.value = MainActivityState.Error(msg = msg)
    }

    private fun getUser() {
        viewModelScope.launch {
            when (val result = firebaseUseCases.getUser()) {
                is Resource.Error -> _state.value = MainActivityState.Error(msg = result.msg)
                is Resource.Success -> _state.value = MainActivityState.GetUser(user = result.data)
            }
        }
    }

    private fun getImage() {
        viewModelScope.launch {
            when (val result = imagesUseCases.getImage()) {
                is Resource.Error -> _state.value = MainActivityState.Error(msg = result.msg)
                is Resource.Success -> _state.value = MainActivityState.Image(image = result.data)
            }
        }
    }

    private fun performDefault() {
        _state.value = MainActivityState.Default
    }

    private fun changeEmail(
        email: String,
        password: String,
        displayName: String,
        newEmail: String
    ) {
        if (email.isBlank() || password.isBlank() || newEmail.isBlank()) {
            showError(msg = "Email, or password, or new email are blank")
            return
        }
        viewModelScope.launch {
            val credential = EmailAuthProvider.getCredential(email, password)
            when (val result = firebaseUseCases.changeEmail(
                credential = credential,
                email = newEmail,
                displayName = displayName
            )) {
                is Resource.Error -> _state.value = MainActivityState.Error(msg = result.msg)
                is Resource.Success -> _state.value =
                    MainActivityState.ChangedEmail(email = email, displayName = displayName)

            }
        }
    }


}