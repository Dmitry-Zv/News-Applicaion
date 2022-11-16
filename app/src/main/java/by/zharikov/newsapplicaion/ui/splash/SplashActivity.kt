package by.zharikov.newsapplicaion.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import by.zharikov.newsapplicaion.databinding.ActivitySplashBinding
import by.zharikov.newsapplicaion.ui.authentication.LoginActivity
import kotlinx.coroutines.*

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private var _binding: ActivitySplashBinding? = null
    private val mBinding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        CoroutineScope(Dispatchers.Main).launch {
            delay(5000)

            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}