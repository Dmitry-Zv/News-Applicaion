package by.zharikov.newsapplicaion.di

import android.content.Context
import android.content.SharedPreferences
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.domain.repository.FirebaseRepository
import by.zharikov.newsapplicaion.domain.usecase.firebaseusecases.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideAuth(): FirebaseAuth =
        Firebase.auth

    @Provides
    @Singleton
    fun provideDatabase(): DatabaseReference =
        Firebase.database.reference

    @Provides
    @Singleton
    fun provideFirebaseStorage(): StorageReference =
        Firebase.storage.reference

    @Provides
    @Singleton
    fun provideGoogleOption(@ApplicationContext context: Context): GoogleSignInOptions =
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.server_client_id))
            .requestEmail()
            .build()

    @Provides
    @Singleton
    fun provideSignInClient(
        @ApplicationContext context: Context,
        options: GoogleSignInOptions
    ): GoogleSignInClient =
        GoogleSignIn.getClient(context, options)

    @Provides
    fun provideLastSignedInAccount(@ApplicationContext context: Context): GoogleSignInAccount? =
        GoogleSignIn.getLastSignedInAccount(context)


    @Provides
    @Singleton
    fun provideFirebaseUseCases(
        repository: FirebaseRepository,
        sharedPreferences: SharedPreferences
    ): FirebaseUseCases =
        FirebaseUseCases(
            changeEmail = ChangeEmail(repository),
            deleteUser = DeleteUser(repository, sharedPreferences),
            getUser = GetUser(repository),
            googleAuth = GoogleAuth(repository, sharedPreferences),
            ifUserLogin = IfUserLogin(repository),
            login = Login(repository, sharedPreferences),
            register = Register(repository),
            resetPassword = ResetPassword(repository),
            signOut = SignOut(repository),
            signInMethod = SignInMethod(sharedPreferences)
        )

}