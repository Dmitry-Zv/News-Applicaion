package by.zharikov.newsapplicaion.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import by.zharikov.newsapplicaion.MainActivity
import by.zharikov.newsapplicaion.databinding.ActivitySplashBinding
import by.zharikov.newsapplicaion.repository.FirebaseRepository
import by.zharikov.newsapplicaion.repository.RegistrationFirebaseRepository
import by.zharikov.newsapplicaion.ui.authentication.login.LoginActivity
import by.zharikov.newsapplicaion.utils.collectLatestLifecycleFlow
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private var _binding: ActivitySplashBinding? = null
    private val mBinding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        val firebaseRepository = FirebaseRepository(this)
        val viewModel = ViewModelProvider(
            this,
            SplashViewModelFactory(firebaseRepository)
        )[SplashViewModel::class.java]
        collectLatestLifecycleFlow(viewModel.uiState) { uiState ->
            Log.d("UI_STATE_SPLASH", uiState.toString())
            delay(2000L)
            when (uiState) {
                is UiStateSplash.UserLoginStateSuccess -> startActivity(
                    Intent(
                        this,
                        MainActivity::class.java
                    )
                )
                is UiStateSplash.UserLoginStateUnSuccess -> startActivity(
                    Intent(
                        this,
                        LoginActivity::class.java
                    )
                )
                is UiStateSplash.Initial -> {}
            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}