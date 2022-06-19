package com.example.clicktorun.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.clicktorun.databinding.ActivityUserDetailsBinding
import com.example.clicktorun.ui.MainActivity
import com.example.clicktorun.utils.startActivityWithAnimation
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserDetailsBinding
    val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpViews()
        setUIListeners()
        setUpViewModelListener()
    }

    private fun setUpViews() {
        authViewModel.username?.let { binding.usernameInput.editText?.setText(it) }
        authViewModel.weight?.let { binding.weightInput.editText?.setText(it) }
        authViewModel.height?.let { binding.heightInput.editText?.setText(it) }
    }

    private fun setUIListeners() {
        binding.usernameInput.editText?.addTextChangedListener { text ->
            binding.usernameInput.isErrorEnabled = false
            authViewModel.username = text?.toString()
        }
        binding.weightInput.editText?.addTextChangedListener { text ->
            binding.weightInput.isErrorEnabled = false
            authViewModel.weight = text?.toString()
        }
        binding.heightInput.editText?.addTextChangedListener { text ->
            binding.heightInput.isErrorEnabled = false
            authViewModel.height = text?.toString()
        }
        binding.btnSubmit.setOnClickListener {
            authViewModel.insertUser()
        }
    }

    private fun setUpViewModelListener() {
        authViewModel.authState.observe(this) {
            when (it) {
                is AuthViewModel.AuthState.Loading -> binding.apply {
                    overlay.visibility = View.VISIBLE
                    progress.visibility = View.VISIBLE
                }
                is AuthViewModel.AuthState.Success -> binding.apply {
                    hideLoading()
                    Intent(this@UserDetailsActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivityWithAnimation(this)
                        finish()
                    }
                }
                is AuthViewModel.AuthState.InvalidUsername -> binding.apply {
                    hideLoading()
                    usernameInput.isErrorEnabled = true
                    usernameInput.error = it.message
                }
                is AuthViewModel.AuthState.InvalidWeight -> binding.apply {
                    hideLoading()
                    weightInput.isErrorEnabled = true
                    weightInput.error = it.message
                }
                is AuthViewModel.AuthState.InvalidHeight -> binding.apply {
                    hideLoading()
                    heightInput.isErrorEnabled = true
                    heightInput.error = it.message
                }
                is AuthViewModel.AuthState.FireBaseFailure -> binding.apply {
                    hideLoading()
                    Snackbar.make(
                        binding.root,
                        it.message ?: "Unknown error has occurred",
                        Snackbar.LENGTH_SHORT
                    ).apply {
                        setAction("OKAY") { dismiss() }
                        show()
                    }
                }
                else -> binding.apply {
                    hideLoading()
                }
            }
        }
    }

    private fun hideLoading() = binding.apply {
        overlay.visibility = View.GONE
        progress.visibility = View.GONE
    }
}