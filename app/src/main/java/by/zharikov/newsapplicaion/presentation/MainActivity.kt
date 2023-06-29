package by.zharikov.newsapplicaion.presentation

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.adapter.SettingAdapter
import by.zharikov.newsapplicaion.connectivity.MyState
import by.zharikov.newsapplicaion.connectivity.NetworkStatusViewModel
import by.zharikov.newsapplicaion.databinding.ActivityMainBinding
import by.zharikov.newsapplicaion.databinding.CustomAlertDialogBinding
import by.zharikov.newsapplicaion.databinding.CustomAlertDialogNewEmailBinding
import by.zharikov.newsapplicaion.databinding.NavHeaderBinding
import by.zharikov.newsapplicaion.domain.model.Settings
import by.zharikov.newsapplicaion.domain.model.User
import by.zharikov.newsapplicaion.presentation.authentication.login.LoginActivity
import by.zharikov.newsapplicaion.utils.*
import com.bumptech.glide.Glide
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ToolBarSetting, SwitchIconClickListener {
    private var _binding: ActivityMainBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var badge: BadgeDrawable
    private lateinit var actionBarToggle: ActionBarDrawerToggle
    private var _customAlertDialogNewEmailBinding: CustomAlertDialogNewEmailBinding? = null
    private val customAlertDialogNewEmailBinding get() = _customAlertDialogNewEmailBinding!!
    private var _customAlertDialogBinding: CustomAlertDialogBinding? = null
    private val customAlertDialogBinding get() = _customAlertDialogBinding!!
    private var _navHeaderBinding: NavHeaderBinding? = null
    private val navHeaderBinding get() = _navHeaderBinding!!
    private lateinit var settingAdapter: SettingAdapter
    private val sharedViewModel: SharedViewModel by viewModels()
    private val networkStatusViewModel: NetworkStatusViewModel by viewModels()
    private val viewModel: MainViewModel by viewModels()

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                try {
                    it.data?.data?.let { uri ->
                        viewModel.onEvent(event = MainActivityEvent.SetImage(image = uri))
                        Glide.with(this).load(uri).into(navHeaderBinding.imageProfile)

                    }
                        ?: viewModel.onEvent(event = MainActivityEvent.ShowError(msg = "Uri is null..."))


                } catch (e: Exception) {
                    viewModel.onEvent(
                        event = MainActivityEvent.ShowError(
                            msg = e.message ?: "Unknown error..."
                        )
                    )
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.findNavController()
        mBinding.bottomNav.setupWithNavController(
            navController = navController
        )
        checkForConnection()
        collectLatestLifecycleFlow(networkStatusViewModel.state) { state ->
            when (state) {
                MyState.Fetched -> {
                    sharedViewModel.setState(state)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Fetched", Toast.LENGTH_SHORT)
                            .show()
                    }
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


        var signInMethod = ""
        collectLatestLifecycleFlow(viewModel.state) { state ->
            when (state) {
                is MainActivityState.ChangedEmail -> changeProfileHeader(
                    email = state.email,
                    displayName = state.displayName
                )
                MainActivityState.Default -> {}
                is MainActivityState.Error -> showSnackBar(msg = state.msg, view = mBinding.root)
                is MainActivityState.GetUser -> initProfile(state.user)
                is MainActivityState.Image -> getImage(state.image)
                MainActivityState.SignOut -> startActivity(Intent(this, LoginActivity::class.java))
                is MainActivityState.SignInMethod -> {

                    signInMethod = state.data

                    when (signInMethod) {
                        Constants.EMAIL_PASSWORD -> {
                            mBinding.navView.inflateMenu(R.menu.drawer_list)
                            sharedViewModel.onEvent(
                                event = SharedViewModelEvent.InitBadgeCounter(
                                    Constants.BADGE_COUNTER_EMAIL_PASSWORD
                                )
                            )
                        }
                        Constants.GOOGLE -> {
                            mBinding.navView.inflateMenu(R.menu.drawer_list_google_authenticator)
                            sharedViewModel.onEvent(
                                event = SharedViewModelEvent.InitBadgeCounter(
                                    Constants.BADGE_COUNTER_GOOGLE
                                )
                            )
                        }
                    }


                }
            }

        }

        _navHeaderBinding = NavHeaderBinding.inflate(layoutInflater)

        mBinding.navView.addHeaderView(navHeaderBinding.root)

        settingAdapter = SettingAdapter(
            Settings.settings,
            this

        )
        navHeaderBinding.settingRecycler.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = settingAdapter
        }
        navHeaderBinding.imageProfile.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            launcher.launch(Intent.createChooser(photoPickerIntent, "Select image from here..."))

        }



        mBinding.navView.setNavigationItemSelectedListener { menuItem ->
            mBinding.drawLayout.closeDrawers()
            when (menuItem.itemId) {
                R.id.change_email -> {
                    _customAlertDialogNewEmailBinding =
                        CustomAlertDialogNewEmailBinding.inflate(layoutInflater)

                    changeEmail()
                }
                R.id.delete_account -> {
                    deleteAccount(signInMethod = signInMethod)

                }
                R.id.sign_out -> {
                    signOut()
                }
            }
            true
        }


        badge = mBinding.bottomNav.getOrCreateBadge(R.id.favourite)
        collectLatestLifecycleFlow(sharedViewModel.articlesState) { articlesCounterState ->
            badge.number = articlesCounterState.data.size
            badge.isVisible = badge.number > 0
        }

    }

    private fun changeProfileHeader(email: String, displayName: String) {
        with(navHeaderBinding) {
            profileEmail.text = email
            profileName.text = displayName
        }
    }

    private fun getImage(image: ByteArray) {
        val bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)
        navHeaderBinding.imageProfile.setImageBitmap(bitmap)
    }

    private fun signOut() {
        Snackbar.make(
            mBinding.root,
            "Are you sure?",
            Snackbar.LENGTH_LONG
        ).setAction("Sing Out") {
            viewModel.onEvent(event = MainActivityEvent.SignOut)
        }.show()
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


    override fun setUpToolBar(title: String, key: String) {
        mBinding.customToolBar.toolBar.title = title
        with(mBinding.customToolBar) {
            when (key) {
                Constants.FRAGMENT_MAIN, Constants.FRAGMENT_SEARCH, Constants.FRAGMENT_DETAILED -> headerCount.visibility =
                    View.INVISIBLE
                Constants.FRAGMENT_FAVOURITE -> {
                    lifecycleScope.launch {
                        sharedViewModel.countItemSave.collect { countItemSave ->
                            if (countItemSave > 0) {
                                headerCount.text = countItemSave.toString()
                                headerCount.visibility = View.VISIBLE

                            } else headerCount.visibility = View.INVISIBLE
                        }
                    }
                }
                else -> {}
            }
        }
        setSupportActionBar(mBinding.customToolBar.toolBar)
        actionBarToggle =
            ActionBarDrawerToggle(this, mBinding.drawLayout, mBinding.customToolBar.toolBar, 0, 0)

        mBinding.drawLayout.addDrawerListener(actionBarToggle)
        actionBarToggle.syncState()


    }

    private fun changeEmail() {
        showAlert(
            title = R.string.change_email,
            view = customAlertDialogNewEmailBinding.root,
            positiveButtonFun = {
                val profileName = navHeaderBinding.profileName.text.toString()
                val email = customAlertDialogNewEmailBinding.inputEmailEt.text.toString()
                val password = customAlertDialogNewEmailBinding.inputPasswordEt.text.toString()
                val newEmail = customAlertDialogNewEmailBinding.newInputEmailEt.text.toString()
                viewModel.onEvent(
                    event = MainActivityEvent.ChangeEmail(
                        email = email,
                        password = password,
                        newEmail = newEmail,
                        displayName = profileName
                    )
                )
            },
            negativeButtonFun = {

            }
        )

    }


    private fun initProfile(user: User) {

        with(navHeaderBinding) {
            profileName.text = user.displayName
            profileEmail.text = user.email
        }
        viewModel.onEvent(event = MainActivityEvent.GetImage)

    }


    private fun deleteAccount(signInMethod: String) {
        when (signInMethod) {
            Constants.EMAIL_PASSWORD -> {
                _customAlertDialogBinding = CustomAlertDialogBinding.inflate(layoutInflater)
                showAlert(
                    title = R.string.delete_account,
                    view = customAlertDialogBinding.root,
                    positiveButtonFun = {
                        val email = customAlertDialogBinding.inputEmailEt.text.toString()
                        val password = customAlertDialogBinding.inputPasswordEt.text.toString()
                        viewModel.onEvent(
                            event = MainActivityEvent.DeleteUser(
                                email = email,
                                password = password
                            )
                        )
                        startActivity(Intent(this, LoginActivity::class.java))
                        sharedViewModel.onEvent(event = SharedViewModelEvent.ResetBadgeCounter)
                    },
                    negativeButtonFun = {

                    }
                )
            }
            Constants.GOOGLE -> {
                showSnackBarWithAction(
                    msg = "Delete account?",
                    mBinding.root,
                    resource = R.string.delete_account
                ) {
                    viewModel.onEvent(event = MainActivityEvent.DeleteUser())
                    startActivity(Intent(this, LoginActivity::class.java))
                    sharedViewModel.onEvent(event = SharedViewModelEvent.ResetBadgeCounter)
                }
            }
        }


    }


    override fun onSwitchIconClickListener(isChecked: Boolean, position: Int) {
        when (position) {
            0 -> {
                sharedViewModel.setStateIsCheckedForPosition0(isChecked)
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }

            }
            1 -> {
                sharedViewModel.setStateIsCheckedForPosition1(isChecked)
            }
        }
    }

}