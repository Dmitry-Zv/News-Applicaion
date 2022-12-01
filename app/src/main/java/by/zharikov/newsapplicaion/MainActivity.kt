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
import by.zharikov.newsapplicaion.databinding.ActivityMainBinding
import by.zharikov.newsapplicaion.databinding.CustomAlertDialogBinding
import by.zharikov.newsapplicaion.databinding.CustomAlertDialogNewEmailBinding
import by.zharikov.newsapplicaion.databinding.NavHeaderBinding
import by.zharikov.newsapplicaion.repository.ArticleEntityRepository
import by.zharikov.newsapplicaion.repository.RegistrationRepository
import by.zharikov.newsapplicaion.repository.UploadDownloadUiArticleOnFirebaseDatabaseRepository
import by.zharikov.newsapplicaion.repository.UploadDownloadImageRepository
import by.zharikov.newsapplicaion.ui.SharedViewModel
import by.zharikov.newsapplicaion.ui.authentication.LoginActivity
import by.zharikov.newsapplicaion.utils.ArticleToEntityArticle
import by.zharikov.newsapplicaion.utils.Constants
import by.zharikov.newsapplicaion.utils.SwitchIconClickListener
import by.zharikov.newsapplicaion.utils.ToolBarSetting
import by.zharikov.newsapplicaion.worker.UploadImageWorker
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), ToolBarSetting, SwitchIconClickListener {
    private var _binding: ActivityMainBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var badge: BadgeDrawable
    private lateinit var repository: RegistrationRepository
    private lateinit var actionBarToggle: ActionBarDrawerToggle
    private var _customAlertDialogNewEmailBinding: CustomAlertDialogNewEmailBinding? = null
    private val customAlertDialogNewEmailBinding get() = _customAlertDialogNewEmailBinding!!
    private var _customAlertDialogBinding: CustomAlertDialogBinding? = null
    private val customAlertDialogBinding get() = _customAlertDialogBinding!!
    private var _navHeaderBinding: NavHeaderBinding? = null
    private val navHeaderBinding get() = _navHeaderBinding!!
    private val articleToEntityArticle = ArticleToEntityArticle()
    private var imageUri: String? = ""
    private lateinit var settingAdapter: SettingAdapter
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
            if (it.resultCode == RESULT_OK) {
                try {
                    val profileImageUri = it.data?.data
                    navHeaderBinding.imageProfile.setImageURI(profileImageUri)

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

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        mBinding.bottomNav.setupWithNavController(
            navController = navHostFragment.findNavController()
        )
        uploadDownloadImageRepository = UploadDownloadImageRepository(this)
        repository = RegistrationRepository(this)
        val articleEntityRepository = ArticleEntityRepository(this)
        mainViewModel = ViewModelProvider(
            this,
            MainViewModelActivityFactory(articleEntityRepository)
        )[MainViewModel::class.java]
        uploadDownloadImageRepository.downloadImage()

        _navHeaderBinding = NavHeaderBinding.inflate(layoutInflater)

        mBinding.navView.addHeaderView(navHeaderBinding.root)

        settingAdapter = SettingAdapter(
            Settings.settings,
            this,
            isChecked0 = pref.getBoolean("SettingBooleanPosition0", false),
            isChecked1 = pref.getBoolean("SettingBooleanPosition1", false)
        )
        navHeaderBinding.settingRecycler.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = settingAdapter
        }
        navHeaderBinding.imageProfile.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            photoPickerIntent.type = "image/*"
            launcher.launch(Intent.createChooser(photoPickerIntent, "Select image from here..."))

        }
        mBinding.navView.setNavigationItemSelectedListener { menuItem ->
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
                    Snackbar.make(
                        mBinding.root,
                        "Are you sure?",
                        Snackbar.LENGTH_LONG
                    ).setAction("Sing Out") {
                        repository.signOut()
                        mainViewModel.getAllArticle()
                        mainViewModel.entityListArticle.observe(this) { entityListArticle ->
                            for (entityArticle in entityListArticle) {
                                pref.edit().putBoolean(entityArticle.title, false).apply()
                            }

                        }
                        mainViewModel.deleteAllArticle()

                        startActivity(Intent(this, LoginActivity::class.java))
                    }.show()
                }
            }
            true
        }
        if (prefFirstRunning.getBoolean("IS_FIRST_RUN", false)) {
            prefFirstRunning.edit().putBoolean("IS_FIRST_RUN", false).apply()
            val uploadDownloadUiArticleOnFirebaseDatabaseRepository =
                UploadDownloadUiArticleOnFirebaseDatabaseRepository(this)
            uploadDownloadUiArticleOnFirebaseDatabaseRepository.downloadUiArticleFromFirebaseDatabase()
            uploadDownloadUiArticleOnFirebaseDatabaseRepository.uiArticleListFromFirebase.observe(
                this
            ) { uiArticleList ->
                for (uiArticle in uiArticleList) {
                    initializeFavourite(uiArticle)
                }
            }
        }
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


    override fun setUpToolBar(title: String, key: String) {
        mBinding.customToolBar.toolBar.title = title
        with(mBinding.customToolBar) {
            when (key) {
                Constants.FRAGMENT_MAIN, Constants.FRAGMENT_SEARCH, Constants.FRAGMENT_DETAILED -> headerCount.visibility =
                    View.INVISIBLE
                Constants.FRAGMENT_FAVOURITE -> {
                    sharedViewModel.countItemSave.observe(this@MainActivity) { countItemSave ->
                        if (countItemSave > 0) {
                            headerCount.text = countItemSave.toString()
                            headerCount.visibility = View.VISIBLE

                        } else headerCount.visibility = View.INVISIBLE
                    }
                }
            }
        }
        setSupportActionBar(mBinding.customToolBar.toolBar)
        actionBarToggle =
            ActionBarDrawerToggle(this, mBinding.drawLayout, mBinding.customToolBar.toolBar, 0, 0)

        mBinding.drawLayout.addDrawerListener(actionBarToggle)
        initProfile()
        actionBarToggle.syncState()


    }

    private fun initializeFavourite(uiArticle: UiArticle) {
        val articleEntityRepository = ArticleEntityRepository(this)
        pref.edit().putBoolean(uiArticle.article.title, uiArticle.isLiked)
            .apply()
        Log.d("idTitle", uiArticle.article.title.toString())
        val entityArticle = uiArticle.article.let { articleToEntityArticle.map(it) }

        if (uiArticle.isLiked) {
            CoroutineScope(Dispatchers.IO).launch {

                articleEntityRepository.repInsertArticle(entityArticle)
            }
            Log.d("Title", entityArticle.title.toString())
            Toast.makeText(this, "SAVED", Toast.LENGTH_SHORT).show()
        }
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
                repository.changeEmail(
                    EmailAuthProvider.getCredential(email, password),
                    newEmail,
                    profileName
                )
                initProfile()
                dialog.dismiss()
            }
            .setNegativeButton("Cansel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    private fun initProfile() {
        repository.getUser()
        repository.user.observe(this) { user ->
            with(navHeaderBinding) {
                profileName.text = user.displayName
                profileEmail.text = user.email
                uploadDownloadImageRepository.data.observe(this@MainActivity) {
                    if (it.isNotEmpty()) {
                        val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                        imageProfile.setImageBitmap(bitmap)
                    } else {
                        imageProfile.setImageResource(R.drawable.profile_image)
                    }
                }
            }


        }
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
                        repository.deleteUser(
                            EmailAuthProvider.getCredential(
                                email,
                                password
                            )
                        )
                        mainViewModel.getAllArticle()
                        mainViewModel.entityListArticle.observe(this) { entityListArticle ->
                            for (entityArticle in entityListArticle) {
                                pref.edit().putBoolean(entityArticle.title, false).apply()
                            }

                        }
                        mainViewModel.deleteAllArticle()
                        repository.deleteFlag.observe(this) {
                            if (it) startActivity(
                                Intent(
                                    this,
                                    LoginActivity::class.java
                                )
                            )
                        }
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
                        repository.deleteUser(
                            GoogleAuthProvider.getCredential(
                                account?.idToken,
                                null
                            )
                        )
                        mainViewModel.getAllArticle()
                        mainViewModel.entityListArticle.observe(this) { entityListArticle ->
                            for (entityArticle in entityListArticle) {
                                pref.edit().putBoolean(entityArticle.title, false).apply()
                            }

                        }
                        mainViewModel.deleteAllArticle()
                        repository.deleteFlag.observe(this) {
                            if (it) startActivity(
                                Intent(
                                    this,
                                    LoginActivity::class.java
                                )
                            )
                        }
                    }
                    .show()


            }
        }
    }

    override fun onSwitchIconClickListener(isChecked: Boolean, position: Int) {
        when (position) {
            0 -> {
                sharedViewModel.setStateIsCheckedForPosition0(isChecked)
                pref.edit().putBoolean("SettingBooleanPosition0", isChecked).apply()
            }
            1 -> {
                sharedViewModel.setStateIsCheckedForPosition1(isChecked)
                pref.edit().putBoolean("SettingBooleanPosition1", isChecked).apply()
            }
        }
    }

    private fun byteArrayInputDataBuilder(data: Uri?): Data {
        return Data.Builder().putString(Constants.KEY_IMAGE, data.toString()).build()
    }


}