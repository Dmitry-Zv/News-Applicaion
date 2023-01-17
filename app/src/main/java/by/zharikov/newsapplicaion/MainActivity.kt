package by.zharikov.newsapplicaion

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import by.zharikov.newsapplicaion.adapter.SettingAdapter
import by.zharikov.newsapplicaion.connectivity.MyState
import by.zharikov.newsapplicaion.connectivity.NetworkStatusTracker
import by.zharikov.newsapplicaion.connectivity.NetworkStatusViewModel
import by.zharikov.newsapplicaion.connectivity.NetworkStatusViewModelFactory
import by.zharikov.newsapplicaion.data.model.Settings
import by.zharikov.newsapplicaion.data.model.UiArticle
import by.zharikov.newsapplicaion.data.model.User
import by.zharikov.newsapplicaion.databinding.ActivityMainBinding
import by.zharikov.newsapplicaion.databinding.CustomAlertDialogBinding
import by.zharikov.newsapplicaion.databinding.CustomAlertDialogNewEmailBinding
import by.zharikov.newsapplicaion.databinding.NavHeaderBinding
import by.zharikov.newsapplicaion.repository.*
import by.zharikov.newsapplicaion.usecase.ArticlePreferencesViewModel
import by.zharikov.newsapplicaion.ui.SharedViewModel
import by.zharikov.newsapplicaion.ui.authentication.login.LoginActivity
import by.zharikov.newsapplicaion.usecase.ArticlePreferencesUseCase
import by.zharikov.newsapplicaion.usecase.ArticlePreferencesViewModelFactory
import by.zharikov.newsapplicaion.utils.Constants
import by.zharikov.newsapplicaion.utils.SwitchIconClickListener
import by.zharikov.newsapplicaion.utils.ToolBarSetting
import by.zharikov.newsapplicaion.utils.collectLatestLifecycleFlow
import by.zharikov.newsapplicaion.worker.UploadImageWorker
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private lateinit var articleViewModel: ArticlePreferencesViewModel
    private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProvider(this)[SharedViewModel::class.java]
    }
    private val viewModel: NetworkStatusViewModel by lazy {
        ViewModelProvider(
            this,
            NetworkStatusViewModelFactory(NetworkStatusTracker(this))
        )[NetworkStatusViewModel::class.java]
    }
    private val prefSetting: SharedPreferences by lazy {
        getSharedPreferences("SETTING_BOOLEAN", Context.MODE_PRIVATE)
    }
    private val pref: SharedPreferences by lazy {
        getSharedPreferences("ARTICLE_PREF_BOOL", Context.MODE_PRIVATE)
    }
    private val prefFirstRunning: SharedPreferences by lazy {
        getSharedPreferences("FIRST_RUNNING", Context.MODE_PRIVATE)
    }
    private val prefSignIn: SharedPreferences by lazy {
        getSharedPreferences("SIGN_STATE", Context.MODE_PRIVATE)
    }
    private lateinit var mainViewModel: MainViewModel
    private lateinit var uploadDownloadImageRepository: UploadDownloadImageRepository

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                try {
                    val profileImageUri = it.data!!.data
                    Glide.with(this).load(profileImageUri).into(navHeaderBinding.imageProfile)
                    val request = OneTimeWorkRequestBuilder<UploadImageWorker>().setInputData(
                        byteArrayInputDataBuilder(profileImageUri)
                    ).build()
                    WorkManager.getInstance(this).enqueue(request)

                } catch (e: Exception) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LIFECYCLE_ACTIVITY", "On Create")
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        mBinding.bottomNav.setupWithNavController(
            navController = navHostFragment.findNavController()
        )
        uploadDownloadImageRepository = UploadDownloadImageRepository(this)

        val articleEntityRepository = ArticleEntityRepository(this)
        val articlePreferencesRepository = ArticlePreferencesRepository(pref)
        val articlePreferencesUseCase =
            ArticlePreferencesUseCase(articlePreferencesRepository, articleEntityRepository)
        val firebaseRepository = FirebaseRepository(this)
        articleViewModel = ViewModelProvider(
            this,
            ArticlePreferencesViewModelFactory(articlePreferencesUseCase)
        )[ArticlePreferencesViewModel::class.java]
        mainViewModel = ViewModelProvider(
            this,
            MainViewModelActivityFactory(firebaseRepository)
        )[MainViewModel::class.java]

        collectLatestLifecycleFlow(mainViewModel.uiState) { uiState ->
            when (uiState) {
                is UiStateAccount.SignOut -> startActivity(Intent(this, LoginActivity::class.java))
                is UiStateAccount.ChangeEmail -> mainViewModel.getUser()
                is UiStateAccount.DeleteAccount -> startActivity(
                    Intent(
                        this,
                        LoginActivity::class.java
                    )
                )
                is UiStateAccount.Error -> Toast.makeText(
                    this,
                    "Error: ${uiState.exception}",
                    Toast.LENGTH_SHORT
                ).show()
                is UiStateAccount.GetUser -> initProfile(user = uiState.user)
                is UiStateAccount.Initial -> {}
            }

        }
        collectLatestLifecycleFlow(uploadDownloadImageRepository.data) { result ->
            when (result) {
                is ResultDownload.Success -> {
                    val bitmap = BitmapFactory.decodeByteArray(result.data, 0, result.data.size)
                    navHeaderBinding.imageProfile.setImageBitmap(bitmap)
                    Toast.makeText(this@MainActivity, "Picture is download", Toast.LENGTH_SHORT)
                        .show()
                }
                is ResultDownload.UnSuccess -> {
                    navHeaderBinding.imageProfile.setImageResource(R.drawable.profile_image)
                    Toast.makeText(
                        this,
                        "Error: ${result.error.message.toString()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is ResultDownload.Initial -> {}
            }
        }

        _navHeaderBinding = NavHeaderBinding.inflate(layoutInflater)

        mBinding.navView.addHeaderView(navHeaderBinding.root)

        settingAdapter = SettingAdapter(
            Settings.settings,
            this,
            isChecked0 = prefSetting.getBoolean("SettingBooleanPosition0", false),
            isChecked1 = prefSetting.getBoolean("SettingBooleanPosition1", false)
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
        if (prefSignIn.getBoolean("SIGN_IN_CHOICE", false)) {
            mBinding.navView.inflateMenu(R.menu.drawer_list)
        } else {
            mBinding.navView.inflateMenu(R.menu.drawer_list_google_authenticator)
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
                    _customAlertDialogBinding = CustomAlertDialogBinding.inflate(layoutInflater)
                    deleteAccount()
                }
                R.id.sign_out -> {
                    signOut()
                }
            }
            true
        }
        if (prefFirstRunning.getBoolean("IS_FIRST_RUN", false)) {
            prefFirstRunning.edit().putBoolean("IS_FIRST_RUN", false).apply()
            val uploadDownloadUiArticleOnFirebaseDatabaseRepository =
                UploadDownloadUiArticleOnFirebaseDatabaseRepository(this)
            uploadDownloadUiArticleOnFirebaseDatabaseRepository.downloadUiArticleFromFirebaseDatabase()
            collectLatestLifecycleFlow(uploadDownloadUiArticleOnFirebaseDatabaseRepository.uiArticleListFromFirebase) { uiArticleList ->
                Log.d("UI_ARTICLE_LIST", uiArticleList.toString())
                for (uiArticle in uiArticleList) {
                    if (uiArticle.article.url != "") initializeFavourite(uiArticle)
                }
            }
        }
        val isChecked0 = prefSetting.getBoolean("SettingBooleanPosition0", false)
        val isChecked1 = prefSetting.getBoolean("SettingBooleanPosition1", false)
        if (isChecked1) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        else {
            if (isChecked0) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }


        badge = mBinding.bottomNav.getOrCreateBadge(R.id.favourite)
        lifecycleScope.launch {
            sharedViewModel.counter.collect { counter ->
                Log.d("INT_COUNTER", counter.toString())
                badge.number = counter
                badge.isVisible = badge.number > 0
            }
        }



        checkForConnection()
        collectLatestLifecycleFlow(viewModel.state) { state ->
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

    }

    private fun signOut() {
        Snackbar.make(
            mBinding.root,
            "Are you sure?",
            Snackbar.LENGTH_LONG
        ).setAction("Sing Out") {
            mainViewModel.signOut()
            articleViewModel.deleteAllArticle()
        }.show()
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        Log.d("LIFECYCLE_ACTIVITY", "On Destroy")
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

    private fun initializeFavourite(uiArticle: UiArticle) {
        articleViewModel.addArticle(uiArticle.article)
        Toast.makeText(this, "SAVED", Toast.LENGTH_SHORT).show()
    }

    private fun changeEmail() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Change Email")
            .setView(customAlertDialogNewEmailBinding.root)
            .setPositiveButton("Ok") { dialog, _ ->
                val profileName = navHeaderBinding.profileName.text.toString()
                val email = customAlertDialogNewEmailBinding.inputEmailEt.text.toString()
                val password = customAlertDialogNewEmailBinding.inputPasswordEt.text.toString()
                val newEmail = customAlertDialogNewEmailBinding.newInputEmailEt.text.toString()
                if (email.isEmpty() || password.isEmpty() || newEmail.isEmpty()) return@setPositiveButton
                mainViewModel.changeAccount(
                    credential = EmailAuthProvider.getCredential(email, password),
                    email = newEmail,
                    profileName = profileName
                )
                dialog.dismiss()
            }
            .setNegativeButton("Cansel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    private fun initProfile(user: User?) {

        with(navHeaderBinding) {
            profileName.text = user?.displayName
            profileEmail.text = user?.email
        }
        uploadDownloadImageRepository.downloadImage()
    }


    private fun deleteAccount() {


        when (prefSignIn.getBoolean("SIGN_IN_CHOICE", false)) {
            true -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Delete account")
                    .setView(customAlertDialogBinding.root)
                    .setPositiveButton("Ok") { _, _ ->
                        val email = customAlertDialogBinding.inputEmailEt.text.toString()
                        val password = customAlertDialogBinding.inputPasswordEt.text.toString()
                        if (email.isEmpty() || password.isEmpty()) return@setPositiveButton
                        mainViewModel.deleteAccount(
                            credential = EmailAuthProvider.getCredential(email, password)
                        )
                        articleViewModel.deleteAllArticle()
                        uploadDownloadImageRepository.deleteImage()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }.show()
            }
            false -> {
                Log.d("CheckSignConnect", "Google")
                Snackbar.make(mBinding.root, "Delete account?", Snackbar.LENGTH_LONG)
                    .setAction("Delete") {
                        val account = GoogleSignIn.getLastSignedInAccount(this)
                        mainViewModel.deleteAccount(
                            credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                        )
                        articleViewModel.deleteAllArticle()
                        uploadDownloadImageRepository.deleteImage()
                    }
                    .show()


            }
        }
    }

    override fun onSwitchIconClickListener(isChecked: Boolean, position: Int) {
        when (position) {
            0 -> {
                sharedViewModel.setStateIsCheckedForPosition0(isChecked)
                prefSetting.edit().putBoolean("SettingBooleanPosition0", isChecked).apply()
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }

            }
            1 -> {
                sharedViewModel.setStateIsCheckedForPosition1(isChecked)
                prefSetting.edit().putBoolean("SettingBooleanPosition1", isChecked).apply()
            }
        }
    }

    private fun byteArrayInputDataBuilder(data: Uri?): Data {
        return Data.Builder().putString(Constants.KEY_IMAGE, data.toString()).build()
    }


}