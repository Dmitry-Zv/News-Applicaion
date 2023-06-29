package by.zharikov.newsapplicaion.domain.usecase.firebaseusecases

data class FirebaseUseCases(
    val changeEmail: ChangeEmail,
    val deleteUser: DeleteUser,
    val getUser: GetUser,
    val googleAuth: GoogleAuth,
    val ifUserLogin: IfUserLogin,
    val login: Login,
    val register: Register,
    val resetPassword: ResetPassword,
    val signOut: SignOut,
    val signInMethod: SignInMethod
)