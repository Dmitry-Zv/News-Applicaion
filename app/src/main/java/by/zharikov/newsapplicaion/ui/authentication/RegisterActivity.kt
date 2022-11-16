package by.zharikov.newsapplicaion.ui.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import by.zharikov.newsapplicaion.R
import by.zharikov.newsapplicaion.databinding.ActivityRegisterBinding
import by.zharikov.newsapplicaion.repository.RegistrationRepository
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private var _binding: ActivityRegisterBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var viewModel: RegistrationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        val registrationRepository = RegistrationRepository(this)
        viewModel = ViewModelProvider(
            this,
            RegistrationViewModelFactory(registrationRepository = registrationRepository)
        )[RegistrationViewModel::class.java]
        mBinding.registerButton.setOnClickListener {
            var job: Job? = null
            job?.cancel()


            job = MainScope().launch {
                mBinding.progressBar.visibility = View.VISIBLE
                delay(500L)
                register()
                mBinding.progressBar.visibility = View.INVISIBLE
            }


        }
        mBinding.textAlreadyHaveAccount.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
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
            viewModel.register(email = email, password = password, userName = userName)
            viewModel.currentUser.observe(this@RegisterActivity) { currentUser ->
                if (currentUser != null) {
                    var job: Job? = null
                    job?.cancel()

                    job = MainScope().launch {
                        Snackbar.make(mBinding.root, "Verification is sent", Snackbar.LENGTH_SHORT)
                            .show()
                        delay(1000)
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    }


                } else
                    Toast.makeText(
                        this@RegisterActivity,
                        "Error: current user is null",
                        Toast.LENGTH_SHORT
                    ).show()

            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}