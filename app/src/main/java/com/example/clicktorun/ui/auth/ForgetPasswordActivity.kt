package com.example.clicktorun.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.clicktorun.databinding.ActivityForgetPasswordBinding
import com.example.clicktorun.utils.endActivityWithAnimation
import com.example.clicktorun.utils.startActivityWithAnimation
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgetPasswordBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpViews()
        setUpListeners()
    }

    private fun setUpViews() {
        authViewModel.email?.let {
            binding.emailInput.editText?.setText(it)
        }
    }

    private fun setUpListeners() {
        binding.apply {
            toolbar.setNavigationOnClickListener { finish() }
            linkSignUp.setOnClickListener {
                startActivityWithAnimation(
                    Intent(
                        this@ForgetPasswordActivity,
                        SignUpActivity::class.java
                    )
                )
            }
            emailInput.editText?.addTextChangedListener {
                emailInput.isErrorEnabled = false
                authViewModel.email = it.toString()
            }
            btnSendEmail.setOnClickListener {
                authViewModel.sendPasswordResetLinkToEmail()
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
                    createSnackbar()
                }
                is AuthViewModel.AuthState.InvalidEmail -> binding.apply {
                    progress.visibility = View.GONE
                    overlay.visibility = View.GONE
                    emailInput.apply {
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
            "Check your email for further instructions on how to reset your password",
            Snackbar.LENGTH_SHORT
        ).apply {
            setAction("OKAY") { dismiss() }
            addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onShown(transientBottomBar: Snackbar?) {
                    super.onShown(transientBottomBar)
                    Handler(mainLooper).postDelayed(
                        {
                            if (!this@ForgetPasswordActivity.isFinishing ||
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

    override fun finish() {
        super.finish()
        endActivityWithAnimation()
    }
}