package by.zharikov.newsapplicaion.ui.authentication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import by.zharikov.newsapplicaion.MainActivity
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.databinding.ActivityLoginBinding
import by.zharikov.newsapplicaion.databinding.CustomAlertDialogRecoveryPasswordBinding
import by.zharikov.newsapplicaion.repository.RegistrationRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var viewModel: LoginViewModel
    private lateinit var customAlertDialogRecoveryPasswordBinding: CustomAlertDialogRecoveryPasswordBinding
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private val prefSignIn: SharedPreferences by lazy {
        getSharedPreferences("SIGN_STATE", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        val registrationRepository = RegistrationRepository(this)
        viewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(registrationRepository)
        )[LoginViewModel::class.java]

        viewModel.ifUserLogged {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {

            val account = GoogleSignIn.getSignedInAccountFromIntent(it.data).result
            account?.let {

                viewModel.googleAuthForFirebaseVM(account) {
                    startActivity(Intent(this, MainActivity::class.java))
                }

            }
        }


        mBinding.buttonLogin.setOnClickListener {
            prefSignIn.edit().putBoolean("SIGN_IN_CHOICE", true).apply()

            var job: Job? = null
            job?.cancel()


            job = MainScope().launch {
                mBinding.progressBar.visibility = View.VISIBLE
                delay(500L)
                login()
                mBinding.progressBar.visibility = View.INVISIBLE
            }

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
            val connectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val isConnected = connectivityManager.activeNetwork != null
            if (isConnected) {
                Log.d("CheckSignConnect", "Google")
                prefSignIn.edit().putBoolean("SIGN_IN_CHOICE", false).apply()
                viewModel.signInWithGoogle()
                viewModel.intent.observe(this) {
                    launcher.launch(it)
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
                viewModel.resetPassword(email)
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
            viewModel.login(email, password, mBinding.root) {
                startActivity(
                    Intent(
                        this@LoginActivity,
                        MainActivity::class.java
                    )
                )
            }


        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}