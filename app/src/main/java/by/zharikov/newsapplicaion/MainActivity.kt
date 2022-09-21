package by.zharikov.newsapplicaion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import by.zharikov.newsapplicaion.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val mBinding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_splash)

        CoroutineScope(Dispatchers.Main)
            .launch {
                delay(5000)
                _binding = ActivityMainBinding.inflate(layoutInflater)
                setContentView(mBinding.root)
                bottom_nav.setupWithNavController(
                    navController = nav_host_fragment.findNavController()
                )
            }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}