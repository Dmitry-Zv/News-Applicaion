package by.zharikov.newsapplicaion.presentation

import by.zharikov.newsapplicaion.domain.model.User

sealed class MainActivityState {
    object Default : MainActivityState()
    data class ChangedEmail(val email: String, val displayName: String) : MainActivityState()

    data class Error(val msg: String) :
        MainActivityState()

    data class Image(val image: ByteArray) :
        MainActivityState() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Image

            if (!image.contentEquals(other.image)) return false

            return true
        }

        override fun hashCode(): Int {
            return image.contentHashCode()
        }
    }

    data class GetUser(val user: User) : MainActivityState()
    object SignOut : MainActivityState()
    data class SignInMethod(val data: String) : MainActivityState()

}