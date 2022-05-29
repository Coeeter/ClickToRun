package com.example.clicktorun.ui.auth

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.clicktorun.databinding.ActivitySignUpBinding
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpListeners()
    }

    private fun setUpListeners() {
        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }
            linkLogin.setOnClickListener { finish() }
            emailInput.editText?.addTextChangedListener {
                emailInput.isErrorEnabled = false
            }
            passwordInput.editText?.addTextChangedListener {
                passwordInput.isErrorEnabled = false
            }
            confirmPasswordInput.editText?.addTextChangedListener {
                confirmPasswordInput.isErrorEnabled = false
            }
            btnSignUp.setOnClickListener {
                val email = emailInput.editText?.text?.toString()
                val password = passwordInput.editText?.text?.toString()
                val confirmPassword = confirmPasswordInput.editText?.text?.toString()
                authViewModel.signUp(email, password, confirmPassword)
            }
        }
        authViewModel.authState.observe(this) {
            when (it) {
                is AuthViewModel.AuthState.Loading -> {
                    binding.progress.visibility = View.VISIBLE
                }
                is AuthViewModel.AuthState.Success -> {
                    binding.progress.visibility = View.GONE
                    createSnackbar()
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
                is AuthViewModel.AuthState.InvalidConfirmPassword -> {
                    binding.progress.visibility = View.GONE
                    binding.confirmPasswordInput.apply {
                        isErrorEnabled = true
                        error = it.message
                    }
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

    private fun createSnackbar() {
        Snackbar.make(
            binding.root,
            "Successfully created account!",
            Snackbar.LENGTH_SHORT
        ).apply {
            setAction("OKAY") { dismiss() }
            addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onShown(transientBottomBar: Snackbar?) {
                    super.onShown(transientBottomBar)
                    Handler(mainLooper).postDelayed(
                        {
                            if (
                                !this@SignUpActivity.isFinishing ||
                                !this@SignUpActivity.isDestroyed
                            ) finish()
                        },
                        3000
                    )
                }

                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    finish()
                }
            })
        }.show()
    }
}