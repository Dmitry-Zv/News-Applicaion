package by.zharikov.newsapplicaion.presentation.authentication.login

data class LoginState(
    val data: String,
    val error: String?,
    val isLoading: Boolean
) {
    companion object {
        val default = LoginState(
            data = "",
            error = null,
            isLoading = false
        )
    }
}

enum class LoginStateName {
    SUCCESS_LOGIN, SUCCESS_LOGIN_WITH_GOOGLE, SUCCESS_RESTORE_PASSWORD, SUCCESS_PRESS_SIGN_UP
}