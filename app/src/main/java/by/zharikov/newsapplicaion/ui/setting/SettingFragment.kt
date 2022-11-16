package by.zharikov.newsapplicaion.ui.setting

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import by.zharikov.newsapplicaion.adapter.SettingAdapter
import by.zharikov.newsapplicaion.data.model.Settings
import by.zharikov.newsapplicaion.databinding.CustomAlertDialogBinding
import by.zharikov.newsapplicaion.databinding.CustomAlertDialogNewEmailBinding
import by.zharikov.newsapplicaion.databinding.FragmentSettingBinding
import by.zharikov.newsapplicaion.repository.RegistrationRepository
import by.zharikov.newsapplicaion.ui.SharedViewModel
import by.zharikov.newsapplicaion.ui.authentication.LoginActivity
import by.zharikov.newsapplicaion.utils.SwitchIconClickListener
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.GoogleAuthProvider


class SettingFragment : Fragment(), SwitchIconClickListener {

    private var _binding: FragmentSettingBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var settingAdapter: SettingAdapter
    private lateinit var repository: RegistrationRepository
    private val pref: SharedPreferences by lazy {
        requireContext().getSharedPreferences("ARTICLE_PREF_BOOL", Context.MODE_PRIVATE)
    }
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var _customAlertDialogBinding: CustomAlertDialogBinding? = null
    private val customAlertDialogBinding get() = _customAlertDialogBinding!!
    private var _customAlertDialogNewEmailBinding: CustomAlertDialogNewEmailBinding? = null
    private val customAlertDialogNewEmailBinding get() = _customAlertDialogNewEmailBinding!!
    private val prefSignIn: SharedPreferences by lazy {
        requireContext().getSharedPreferences("SIGN_STATE", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingAdapter = SettingAdapter(
            Settings.settings,
            this,
            isChecked0 = pref.getBoolean("SettingBooleanPosition0", false),
            isChecked1 = pref.getBoolean("SettingBooleanPosition1", false)
        )
        mBinding.settingRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = settingAdapter
        }

        repository = RegistrationRepository(requireContext())
        initProfile()
        mBinding.signOutButton.setOnClickListener {
            Snackbar.make(
                mBinding.root,
                "Are you sure?",
                Snackbar.LENGTH_LONG
            ).setAction("Sing Out") {
                repository.signOut()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }.show()

        }
        mBinding.deleteAccountButton.setOnClickListener {
            _customAlertDialogBinding = CustomAlertDialogBinding.inflate(layoutInflater)
            deleteAccount()
        }
        mBinding.changeEmailButton.setOnClickListener {
            _customAlertDialogNewEmailBinding =
                CustomAlertDialogNewEmailBinding.inflate(layoutInflater)
            changeEmail()
        }


    }

    private fun changeEmail() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Email")
            .setView(customAlertDialogNewEmailBinding.root)
            .setPositiveButton("Ok") { dialog, _ ->
                val email = customAlertDialogNewEmailBinding.inputEmailEt.text.toString()
                val password = customAlertDialogNewEmailBinding.inputPasswordEt.text.toString()
                val newEmail = customAlertDialogNewEmailBinding.newInputEmailEt.text.toString()
                if (email.isEmpty() || password.isEmpty() || newEmail.isEmpty()) return@setPositiveButton
                repository.changeEmail(EmailAuthProvider.getCredential(email, password), newEmail)
                dialog.dismiss()
            }
            .setNegativeButton("Cansel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun initProfile() {
        repository.getUser()
        repository.user.observe(viewLifecycleOwner) { user ->
            with(mBinding) {
                profileName.text = user.displayName
                profileEmail.text = user.email
            }
        }


    }

    private fun deleteAccount() {

        when (prefSignIn.getBoolean("SIGN_IN_CHOICE", false)) {
            true -> {
                MaterialAlertDialogBuilder(requireContext())
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
                        repository.deleteFlag.observe(viewLifecycleOwner) {
                            if (it) startActivity(
                                Intent(
                                    requireContext(),
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
                val account = GoogleSignIn.getLastSignedInAccount(requireContext())
                repository.deleteUser(
                    GoogleAuthProvider.getCredential(
                        account?.idToken,
                        null
                    )
                )

                repository.deleteFlag.observe(viewLifecycleOwner) {
                    if (it) startActivity(
                        Intent(
                            requireContext(),
                            LoginActivity::class.java
                        )
                    )
                }


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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}