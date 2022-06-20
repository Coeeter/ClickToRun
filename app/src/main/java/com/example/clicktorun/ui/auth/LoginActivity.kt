package com.example.clicktorun.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.clicktorun.databinding.ActivityLoginBinding
import com.example.clicktorun.ui.MainActivity
import com.example.clicktorun.utils.createSnackBar
import com.example.clicktorun.utils.startActivityWithAnimation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpViews()
        setUpListeners()
    }

    private fun setUpViews() {
        authViewModel.email?.let {
            binding.emailInput.editText?.setText(it)
        }
        authViewModel.password?.let {
            binding.passwordInput.editText?.setText(it)
        }
    }

    private fun setUpListeners() {
        binding.apply {
            emailInput.editText?.addTextChangedListener {
                emailInput.isErrorEnabled = false
                authViewModel.email = it.toString()
            }
            passwordInput.editText?.addTextChangedListener {
                passwordInput.isErrorEnabled = false
                authViewModel.password = it.toString()
            }
            btnLogin.setOnClickListener { authViewModel.logIn() }
            linkSignUp.setOnClickListener {
                startActivityWithAnimation(
                    Intent(
                        this@LoginActivity,
                        SignUpActivity::class.java
                    )
                )
            }
            linkForgetPassword.setOnClickListener {
                startActivityWithAnimation(
                    Intent(
                        this@LoginActivity,
                        ForgetPasswordActivity::class.java
                    )
                )
            }
        }
        authViewModel.authState.observe(this) {
            when (it) {
                is AuthViewModel.AuthState.Loading -> binding.apply {
                    progress.visibility = View.VISIBLE
                }
                is AuthViewModel.AuthState.Success -> binding.apply {
                    progress.visibility = View.GONE
                    checkUserStatus()
                }
                is AuthViewModel.AuthState.FireBaseFailure -> binding.apply {
                    progress.visibility = View.GONE
                    binding.root.createSnackBar(
                        message = it.message ?: "Unknown error has occurred",
                        okayAction = true
                    ).show()
                }
                is AuthViewModel.AuthState.InvalidEmail -> binding.apply {
                    progress.visibility = View.GONE
                    emailInput.apply {
                        isErrorEnabled = true
                        error = it.message
                    }
                }
                is AuthViewModel.AuthState.InvalidPassword -> binding.apply {
                    progress.visibility = View.GONE
                    passwordInput.apply {
                        isErrorEnabled = true
                        error = it.message
                    }
                }
                else -> binding.apply {
                    progress.visibility = View.GONE
                }
            }
        }
    }

    private fun checkUserStatus() {
        CoroutineScope(Dispatchers.Main).launch {
            val state = authViewModel.getCurrentUserState()
            if (state["firestoreUser"] == null)
                return@launch Intent(this@LoginActivity, UserDetailsActivity::class.java).run {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivityWithAnimation(this)
                    finish()
                }
            Intent(this@LoginActivity, MainActivity::class.java).run {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivityWithAnimation(this)
                finish()
            }
        }
    }
}