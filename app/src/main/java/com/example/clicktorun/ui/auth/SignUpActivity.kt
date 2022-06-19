package com.example.clicktorun.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.clicktorun.databinding.ActivitySignUpBinding
import com.example.clicktorun.utils.endActivityWithAnimation
import com.example.clicktorun.utils.startActivityWithAnimation
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
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
        authViewModel.confirmPassword?.let {
            binding.confirmPasswordInput.editText?.setText(it)
        }
    }

    private fun setUpListeners() {
        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }
            linkLogin.setOnClickListener {
                startActivityWithAnimation(
                    Intent(
                        this@SignUpActivity,
                        LoginActivity::class.java
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )
            }
            emailInput.editText?.addTextChangedListener {
                emailInput.isErrorEnabled = false
                authViewModel.email = it.toString()
            }
            passwordInput.editText?.addTextChangedListener {
                passwordInput.isErrorEnabled = false
                confirmPasswordInput.isErrorEnabled = false
                authViewModel.password = it.toString()
            }
            confirmPasswordInput.editText?.addTextChangedListener {
                confirmPasswordInput.isErrorEnabled = false
                passwordInput.isErrorEnabled = false
                authViewModel.confirmPassword = it.toString()
            }
            btnSignUp.setOnClickListener { authViewModel.signUp() }
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
                    createSnackbar()
                }
                is AuthViewModel.AuthState.FireBaseFailure -> binding.apply {
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
                is AuthViewModel.AuthState.InvalidConfirmPassword -> binding.apply {
                    progress.visibility = View.GONE
                    overlay.visibility = View.GONE
                    confirmPasswordInput.apply {
                        isErrorEnabled = true
                        error = it.message
                    }
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

    override fun finish() {
        super.finish()
        endActivityWithAnimation()
    }
}