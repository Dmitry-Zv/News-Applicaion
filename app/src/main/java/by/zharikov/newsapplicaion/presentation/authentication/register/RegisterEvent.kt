package by.zharikov.newsapplicaion.presentation.authentication.register

sealed class RegisterEvent {
    data class OnRegister(
        val email: String,
        val password: String,
        val repeatPassword: String,
        val displayName: String
    ) : RegisterEvent()

    object Default : RegisterEvent()

    object OnAlreadyHaveAnAccount : RegisterEvent()
}
