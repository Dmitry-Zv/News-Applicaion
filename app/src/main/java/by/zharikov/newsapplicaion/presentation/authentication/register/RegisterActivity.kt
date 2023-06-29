package by.zharikov.newsapplicaion.presentation.authentication.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import by.zharikov.newsapplicaion.databinding.ActivityRegisterBinding
import by.zharikov.newsapplicaion.presentation.authentication.login.LoginActivity
import by.zharikov.newsapplicaion.utils.collectLatestLifecycleFlow
import by.zharikov.newsapplicaion.utils.showSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {
    private var _binding: ActivityRegisterBinding? = null
    private val mBinding get() = _binding!!
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(mBinding.root)


        mBinding.registerButton.setOnClickListener {
            register()

        }
        mBinding.textAlreadyHaveAccount.setOnClickListener {
            viewModel.onEvent(event = RegisterEvent.OnAlreadyHaveAnAccount)
        }

        collectLatestLifecycleFlow(viewModel.state) { state ->
            when {
                state.isLoading -> mBinding.progressBar.visibility = View.VISIBLE

                state.error != null -> {
                    showSnackBar(msg = state.error, view = mBinding.root)
                    mBinding.progressBar.visibility = View.GONE
                }

                state.data.isNotBlank() -> {
                    when (state.data) {
                        RegisterStateName.REGISTER.name -> {
                            startActivity(
                                Intent(
                                    this,
                                    LoginActivity::class.java
                                )
                            )
                            showSnackBar(
                                msg = "Verification message has been sent on your email!",
                                view = mBinding.root
                            )
                        }
                        RegisterStateName.ALREADY_HAVE_AN_ACCOUNT.name -> startActivity(
                            Intent(
                                this,
                                LoginActivity::class.java
                            )
                        )
                    }
                }

            }
        }

    }

    private fun register() {
        mBinding.apply {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()
            val repeatPassword = inputConformPassword.text.toString()
            val userName = inputUsername.text.toString()

            viewModel.onEvent(
                event = RegisterEvent.OnRegister(
                    email = email,
                    password = password,
                    repeatPassword = repeatPassword,
                    displayName = userName
                )
            )
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}