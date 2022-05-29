package com.example.clicktorun.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import com.example.clicktorun.databinding.ActivityForgetPasswordBinding
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

class ForgetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgetPasswordBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpListeners()
    }

    private fun setUpListeners() {
        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }
            linkSignUp.setOnClickListener {
                Intent(this@ForgetPasswordActivity, SignUpActivity::class.java).let {
                    startActivity(it)
                }
            }
            emailInput.editText?.addTextChangedListener {
                emailInput.isErrorEnabled = false
            }
            btnSendEmail.setOnClickListener {
                val email = emailInput.editText?.text?.toString()
                authViewModel.sendPasswordResetLinkToEmail(email)
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
                is AuthViewModel.AuthState.InvalidEmail -> {
                    binding.progress.visibility = View.GONE
                    binding.emailInput.apply {
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
            "Check your email for further instructions on how to reset your password",
            Snackbar.LENGTH_SHORT
        ).apply {
            setAction("OKAY") { dismiss() }
            addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onShown(transientBottomBar: Snackbar?) {
                    super.onShown(transientBottomBar)
                    Handler(mainLooper).postDelayed(
                        {
                            if (
                                !this@ForgetPasswordActivity.isFinishing ||
                                !this@ForgetPasswordActivity.isDestroyed
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