package by.zharikov.newsapplicaion.presentation.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import by.zharikov.newsapplicaion.presentation.MainActivity
import by.zharikov.newsapplicaion.databinding.ActivitySplashBinding
import by.zharikov.newsapplicaion.presentation.authentication.login.LoginActivity
import by.zharikov.newsapplicaion.utils.collectLatestLifecycleFlow
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private var _binding: ActivitySplashBinding? = null
    private val mBinding get() = _binding!!
    private val viewModel: SplashViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        viewModel.onEvent(event = SplashEvent.CheckUserLogin)
        collectLatestLifecycleFlow(viewModel.state) { state ->
            when (state.state) {
                SplashStateName.SUCCESS.name -> {
                    startActivity(
                        Intent(
                            this,
                            MainActivity::class.java
                        )
                    )
                    finish()
                }
                SplashStateName.ERROR.name -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}