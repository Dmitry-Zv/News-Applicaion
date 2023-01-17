package by.zharikov.newsapplicaion.ui.authentication.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.databinding.ActivityRegisterBinding
import by.zharikov.newsapplicaion.repository.RegistrationFirebaseRepository
import by.zharikov.newsapplicaion.ui.authentication.login.LoginActivity
import by.zharikov.newsapplicaion.usecase.registration_firebase_use_case.RegistrationFirebaseUseCase
import by.zharikov.newsapplicaion.utils.collectLatestLifecycleFlow
import com.google.android.material.snackbar.Snackbar

class RegisterActivity : AppCompatActivity() {
    private var _binding: ActivityRegisterBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        val registrationRepository = RegistrationFirebaseRepository(this)
        val registerFirebaseUseCase = RegistrationFirebaseUseCase(registrationRepository)
        viewModel = ViewModelProvider(
            this,
            RegisterViewModelFactory(registerFirebaseUseCase)
        )[RegisterViewModel::class.java]
        mBinding.registerButton.setOnClickListener {
            register()

        }
        mBinding.textAlreadyHaveAccount.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        collectLatestLifecycleFlow(viewModel.uiState) { uiState ->
            when (uiState) {
                is UiStateRegister.Initial -> {}
                is UiStateRegister.Loading -> mBinding.progressBar.visibility = View.VISIBLE
                is UiStateRegister.ShowNotification -> {
                    mBinding.progressBar.visibility = View.GONE
                    Snackbar.make(mBinding.root, "Verification is sent!", Snackbar.LENGTH_SHORT)
                        .show()
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                }
                is UiStateRegister.ShowError -> {
                    mBinding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@RegisterActivity,
                        "Error: ${uiState.exception}",
                        Toast.LENGTH_SHORT
                    ).show()
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

            if (userName.isEmpty() && email.isEmpty() && password.isEmpty() && repeatPassword.isEmpty()) {
                inputUsername.error = "First name is required!"
                inputUsername.requestFocus()
                inputEmail.error = getString(R.string.email_required)
                inputEmail.requestFocus()
                inputPassword.error = getString(R.string.password_required)
                inputPassword.requestFocus()
                inputConformPassword.error = "Conform password is empty!"
                inputConformPassword.requestFocus()
                return
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                inputEmail.error = "Please provide valid email!"
                inputEmail.requestFocus()
                return
            }

            if (password.length < 8) {
                inputPassword.error = "Minimal length of password doesn't match!"
                inputPassword.requestFocus()
                return
            }


            if (repeatPassword != password) {
                inputConformPassword.error = "Conform password doesn't match password!"
                inputConformPassword.requestFocus()
                return
            }
            viewModel.registerAuth(
                email = email,
                password = password,
                displayName = userName
            )
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}