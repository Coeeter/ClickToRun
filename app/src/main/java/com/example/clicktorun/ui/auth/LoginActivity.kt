package com.example.clicktorun.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.clicktorun.databinding.ActivityLoginBinding
import com.example.clicktorun.ui.MainActivity
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
                startActivity(
                    Intent(
                        this@LoginActivity,
                        SignUpActivity::class.java
                    )
                )
            }
            linkForgetPassword.setOnClickListener {
                startActivity(
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
                    overlay.visibility = View.VISIBLE
                }
                is AuthViewModel.AuthState.Success -> binding.apply {
                    progress.visibility = View.GONE
                    overlay.visibility = View.GONE
                    Intent(
                        this@LoginActivity,
                        MainActivity::class.java
                    ).run {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(this)
                        finish()
                    }
                }
                is AuthViewModel.AuthState.Failure -> binding.apply {
                    progress.visibility = View.GONE
                    overlay.visibility = View.GONE
                    Snackbar.make(
                        binding.root,
                        it.message ?: "Unknown error has occurred",
                        Snackbar.LENGTH_SHORT
                    ).apply {
                        setAction("OKAY") { dismiss() }
                        show()
                    }
                }
                is AuthViewModel.AuthState.InvalidEmail -> binding.apply {
                    progress.visibility = View.GONE
                    overlay.visibility = View.GONE
                    emailInput.apply {
                        isErrorEnabled = true
                        error = it.message
                    }
                }
                is AuthViewModel.AuthState.InvalidPassword -> binding.apply {
                    progress.visibility = View.GONE
                    overlay.visibility = View.GONE
                    passwordInput.apply {
                        isErrorEnabled = true
                        error = it.message
                    }
                }
                else -> binding.apply {
                    progress.visibility = View.GONE
                    overlay.visibility = View.GONE
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