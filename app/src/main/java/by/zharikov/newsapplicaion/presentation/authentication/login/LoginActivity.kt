package by.zharikov.newsapplicaion.presentation.authentication.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import by.zharikov.newsapplicaion.presentation.MainActivity
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.databinding.ActivityLoginBinding
import by.zharikov.newsapplicaion.databinding.CustomAlertDialogRecoveryPasswordBinding
import by.zharikov.newsapplicaion.presentation.authentication.register.RegisterActivity
import by.zharikov.newsapplicaion.utils.collectLatestLifecycleFlow
import by.zharikov.newsapplicaion.utils.showAlert
import by.zharikov.newsapplicaion.utils.showSnackBar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private var _binding: ActivityLoginBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var customAlertDialogRecoveryPasswordBinding: CustomAlertDialogRecoveryPasswordBinding
    private lateinit var launcher: ActivityResultLauncher<Intent>

    private val viewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var client: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        collectLatestLifecycleFlow(viewModel.state) { state ->
            when {
                state.isLoading -> mBinding.progressBar.visibility = View.VISIBLE

                state.error != null -> {
                    showSnackBar(msg = state.error, view = mBinding.root)
                    mBinding.progressBar.visibility = View.GONE
                }

                state.data.isNotBlank() -> {
                    when (state.data) {
                        LoginStateName.SUCCESS_LOGIN.name, LoginStateName.SUCCESS_LOGIN_WITH_GOOGLE.name -> {
                            startActivity(
                                Intent(
                                    this,
                                    MainActivity::class.java
                                )
                            )
                            finish()
                        }

                        LoginStateName.SUCCESS_PRESS_SIGN_UP.name -> {
                            startActivity(
                                Intent(
                                    this,
                                    RegisterActivity::class.java
                                )
                            )
                            finish()
                        }
                        LoginStateName.SUCCESS_RESTORE_PASSWORD.name -> {
                            mBinding.progressBar.visibility = View.GONE
                            showSnackBar(
                                msg = "Restore password was send",
                                view = mBinding.root
                            )
                        }

                    }
                }

            }
        }


        launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            val account = GoogleSignIn.getSignedInAccountFromIntent(it.data).result
            viewModel.onEvent(event = LoginEvent.OnLoginWithGoogle(account = account))
        }


        mBinding.buttonLogin.setOnClickListener {
            login()
        }
        mBinding.signUpText.setOnClickListener {
            viewModel.onEvent(event = LoginEvent.OnSignUp)
        }
        mBinding.forgotPasswordText.setOnClickListener {
            customAlertDialogRecoveryPasswordBinding =
                CustomAlertDialogRecoveryPasswordBinding.inflate(layoutInflater)
            resetPassword()
        }

        mBinding.signWithGoogle.setOnClickListener {
            lifecycleScope.launch {
                launcher.launch(client.signInIntent)
            }
        }
    }


    private fun resetPassword() {
        showAlert(
            title = R.string.delete_account,
            view = customAlertDialogRecoveryPasswordBinding.root,
            positiveButtonFun = {
                val email = customAlertDialogRecoveryPasswordBinding.inputEmailEt.text.toString()
                viewModel.onEvent(LoginEvent.OnResetPassword(email = email))
            },
            negativeButtonFun = {

            }
        )
    }

    private fun login() {
        mBinding.apply {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()

            viewModel.onEvent(event = LoginEvent.OnLogin(email = email, password = password))
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}