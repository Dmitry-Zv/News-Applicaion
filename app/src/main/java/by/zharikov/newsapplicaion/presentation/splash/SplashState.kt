package by.zharikov.newsapplicaion.presentation.splash

data class SplashState(
    val state: String
) {
    companion object {
        val default = SplashState(state = SplashStateName.DEFAULT.name)
    }
}

enum class SplashStateName {
    DEFAULT, ERROR, SUCCESS
}