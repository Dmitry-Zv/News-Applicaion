package by.zharikov.newsapplicaion

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.ui.setupWithNavController
import by.zharikov.newsapplicaion.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import by.zharikov.newsapplicaion.connectivity.MyState
import by.zharikov.newsapplicaion.connectivity.NetworkStatusTracker
import by.zharikov.newsapplicaion.connectivity.NetworkStatusViewModel
import by.zharikov.newsapplicaion.connectivity.NetworkStatusViewModelFactory
import by.zharikov.newsapplicaion.ui.SharedViewModel
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var badge: BadgeDrawable
    private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProvider(this)[SharedViewModel::class.java]
    }
    private val viewModel: NetworkStatusViewModel by lazy {
        ViewModelProvider(
            this,
            NetworkStatusViewModelFactory(NetworkStatusTracker(this))
        )[NetworkStatusViewModel::class.java]
    }
    private val pref: SharedPreferences by lazy {
        getSharedPreferences("ARTICLE_PREF_BOOL", Context.MODE_PRIVATE)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_splash)

        CoroutineScope(Dispatchers.Main)
            .launch {
                delay(5000)
                _binding = ActivityMainBinding.inflate(layoutInflater)
                setContentView(mBinding.root)
                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                mBinding.bottomNav.setupWithNavController(
                    navController = navHostFragment.findNavController()
                )
                val isChecked0 = pref.getBoolean("SettingBooleanPosition0", false)
                val isChecked1 = pref.getBoolean("SettingBooleanPosition1", false)
                if (isChecked1) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                else {
                    if (isChecked0) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }

                badge = mBinding.bottomNav.getOrCreateBadge(R.id.favourite)
                sharedViewModel.counter.observe(this@MainActivity) { counter ->
                    badge.number = counter
                    badge.isVisible = badge.number > 0
                }


            }
        checkForConnection()
        viewModel.state.observe(this) { state ->

            when (state) {
                MyState.Fetched -> {
                    sharedViewModel.setState(state)
                    Toast.makeText(this, "Fetched", Toast.LENGTH_SHORT).show()
                }

                MyState.Lost -> {
                    sharedViewModel.setState(state)
                    Snackbar.make(
                        mBinding.navHostFragment,
                        "Lost internet connection",
                        Snackbar.LENGTH_SHORT
                    )
                        .show()

                }

            }
        }
        sharedViewModel.isCheckedPosition1.observe(this) { isChecked1 ->
            if (isChecked1) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        }
        sharedViewModel.isCheckedPosition0.observe(this) { isChecked0 ->
            Log.d("SwitchChecked", isChecked0.toString())
            val isChecked = pref.getBoolean("SettingBooleanPosition1", false)

            if (!isChecked) {
                if (isChecked0) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }


    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun checkForConnection() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val isConnected = connectivityManager.activeNetwork != null
        if (!isConnected)
            sharedViewModel.setState(MyState.Lost)


    }
}