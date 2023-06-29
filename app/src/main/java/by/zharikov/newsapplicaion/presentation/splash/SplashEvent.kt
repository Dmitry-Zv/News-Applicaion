package by.zharikov.newsapplicaion.presentation.splash

sealed class SplashEvent {
    object CheckUserLogin : SplashEvent()
    object Default : SplashEvent()
}