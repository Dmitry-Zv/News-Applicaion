package by.zharikov.newsapplicaion.ui.authentication.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import by.zharikov.newsapplicaion.MainActivity
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.databinding.ActivityLoginBinding
import by.zharikov.newsapplicaion.databinding.CustomAlertDialogRecoveryPasswordBinding
import by.zharikov.newsapplicaion.repository.FirebaseRepository
import by.zharikov.newsapplicaion.repository.RegistrationFirebaseRepository
import by.zharikov.newsapplicaion.ui.authentication.register.RegisterActivity
import by.zharikov.newsapplicaion.utils.collectLatestLifecycleFlow
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var customAlertDialogRecoveryPasswordBinding: CustomAlertDialogRecoveryPasswordBinding
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var registrationRepository: RegistrationFirebaseRepository
    private val prefSignIn: SharedPreferences by lazy {
        getSharedPreferences("SIGN_STATE", Context.MODE_PRIVATE)
    }
    private val prefFirstRunning: SharedPreferences by lazy {
        getSharedPreferences("FIRST_RUNNING", Context.MODE_PRIVATE)
    }
    private lateinit var loginViewModel: LoginViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("LIFECYCLE_ACTIVITY", "on Create LoginActivity")
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        val firebaseRepository = FirebaseRepository(this)
        registrationRepository = RegistrationFirebaseRepository(this)
        loginViewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(firebaseRepository)
        )[LoginViewModel::class.java]


        collectLatestLifecycleFlow(loginViewModel.uiState) { uiState ->
            when (uiState) {
                is UiStateFirebaseLogin.Login -> {
                    mBinding.progressBar.visibility = View.GONE
                    if (uiState.currentUser.isEmailVerified) {
                        startActivity(
                            Intent(
                                this,
                                MainActivity::class.java
                            )
                        )
                    } else Snackbar.make(
                        mBinding.root,
                        "Email is not verified!",
                        Snackbar.LENGTH_SHORT
                    ).show()

                }
                is UiStateFirebaseLogin.LoginWithGoogle -> {
                    startActivity(
                        Intent(
                            this,
                            MainActivity::class.java
                        )
                    )
                }
                is UiStateFirebaseLogin.ResetPassword -> Snackbar.make(
                    mBinding.root,
                    "Reset password was sent!",
                    Snackbar.LENGTH_SHORT
                ).show()
                is UiStateFirebaseLogin.Error -> {
                    mBinding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this,
                        "Error: ${uiState.exception}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is UiStateFirebaseLogin.Initial -> {}
                is UiStateFirebaseLogin.Load -> mBinding.progressBar.visibility = View.VISIBLE
            }
        }


        launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {

            val account = GoogleSignIn.getSignedInAccountFromIntent(it.data).result
            account?.let {
                loginViewModel.loginWithGoogle(account = account)

            }
        }


        mBinding.buttonLogin.setOnClickListener {
            prefSignIn.edit().putBoolean("SIGN_IN_CHOICE", true).apply()
            prefFirstRunning.edit().putBoolean("IS_FIRST_RUN", true).apply()
            login()

        }
        mBinding.signUpText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        mBinding.forgotPasswordText.setOnClickListener {
            customAlertDialogRecoveryPasswordBinding =
                CustomAlertDialogRecoveryPasswordBinding.inflate(layoutInflater)
            resetPassword()
        }

        mBinding.signWithGoogle.setOnClickListener {
            prefFirstRunning.edit().putBoolean("IS_FIRST_RUN", true).apply()
            val connectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val isConnected = connectivityManager.activeNetwork != null
            if (isConnected) {

                Log.d("CheckSignConnect", "Google")
                prefSignIn.edit().putBoolean("SIGN_IN_CHOICE", false).apply()
                firebaseRepository.signInWithGoogle()
                lifecycleScope.launch {
                    firebaseRepository.intent.collect {
                        launcher.launch(it)
                    }
                }


            } else {
                Snackbar.make(mBinding.root, "Connection is lost!", Snackbar.LENGTH_SHORT)
                    .show()
            }

        }
    }

    private fun resetPassword() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete account")
            .setView(customAlertDialogRecoveryPasswordBinding.root)
            .setPositiveButton("Ok") { dialog, _ ->
                val email = customAlertDialogRecoveryPasswordBinding.inputEmailEt.text.toString()
                loginViewModel.resetPassword(email = email)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun login() {
        mBinding.apply {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()
            if (email.isEmpty() && password.isEmpty()) {
                inputEmail.error = getString(R.string.email_required)
                inputEmail.requestFocus()
                inputPassword.error = getString(R.string.password_required)
                inputPassword.requestFocus()
                return
            }
            loginViewModel.login(email = email, password = password)
        }
    }


    override fun onDestroy() {
        Log.d("LIFECYCLE_ACTIVITY", "on Destroy LoginActivity")
        super.onDestroy()
        _binding = null
    }

}