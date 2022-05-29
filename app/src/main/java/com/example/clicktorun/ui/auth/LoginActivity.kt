package com.example.clicktorun.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.clicktorun.ui.MainActivity
import com.example.clicktorun.databinding.ActivityLoginBinding
import com.example.clicktorun.utils.Constants.Companion.ACTION_ANIMATE_LOGIN_PAGE
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        animateScreen()
        setUpListeners()
    }

    private fun setUpListeners() {
        binding.apply {
            emailInput.editText?.addTextChangedListener {
                emailInput.isErrorEnabled = false
            }
            passwordInput.editText?.addTextChangedListener {
                passwordInput.isErrorEnabled = false
            }
            btnLogin.setOnClickListener {
                val email = emailInput.editText?.text?.toString()
                val password = passwordInput.editText?.text?.toString()
                authViewModel.logIn(email, password)
            }
            linkSignUp.setOnClickListener {
                Intent(this@LoginActivity, SignUpActivity::class.java).also {
                    startActivity(it)
                }
            }
            linkForgetPassword.setOnClickListener {
                Intent(this@LoginActivity, ForgetPasswordActivity::class.java).also {
                    startActivity(it)
                }
            }
        }
        authViewModel.authState.observe(this) {
            when (it) {
                is AuthViewModel.AuthState.Loading -> {
                    binding.progress.visibility = View.VISIBLE
                }
                is AuthViewModel.AuthState.Success -> {
                    binding.progress.visibility = View.GONE
                    Intent(this, MainActivity::class.java).run {
                        startActivity(this)
                        finish()
                    }
                }
                is AuthViewModel.AuthState.Failure -> {
                    binding.progress.visibility = View.GONE
                    Snackbar.make(
                        binding.root,
                        it.message ?: "Unknown error has occurred",
                        Snackbar.LENGTH_SHORT
                    ).apply {
                        setAction("OKAY") { dismiss() }
                        show()
                    }
                }
                is AuthViewModel.AuthState.InvalidEmail -> {
                    binding.progress.visibility = View.GONE
                    binding.emailInput.apply {
                        isErrorEnabled = true
                        error = it.message
                    }
                }
                is AuthViewModel.AuthState.InvalidPassword -> {
                    binding.progress.visibility = View.GONE
                    binding.passwordInput.apply {
                        isErrorEnabled = true
                        error = it.message
                    }
                }
                else -> {
                    binding.progress.visibility = View.GONE
                }
            }
        }
    }

    private fun animateScreen() {
        if (intent.action == ACTION_ANIMATE_LOGIN_PAGE)
            return run {
                Handler(mainLooper).postDelayed({
                    binding.screen.visibility = View.VISIBLE
                }, 1000)
            }
        binding.screen.visibility = View.VISIBLE
    }
}