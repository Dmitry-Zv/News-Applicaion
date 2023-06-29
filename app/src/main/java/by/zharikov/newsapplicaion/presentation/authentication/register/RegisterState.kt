package by.zharikov.newsapplicaion.presentation.authentication.register

data class RegisterState(
    val data: String,
    val error: String?,
    val isLoading: Boolean
) {
    companion object {
        val default = RegisterState(
            data = "",
            error = null,
            isLoading = false
        )
    }
}

enum class RegisterStateName {
    REGISTER, ALREADY_HAVE_AN_ACCOUNT
}
