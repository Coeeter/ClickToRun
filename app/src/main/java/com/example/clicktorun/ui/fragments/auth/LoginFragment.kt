package com.example.clicktorun.ui.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.clicktorun.R
import com.example.clicktorun.databinding.FragmentLoginBinding
import com.example.clicktorun.ui.activities.AuthActivity
import com.example.clicktorun.ui.activities.MainActivity
import com.example.clicktorun.ui.viewmodels.AuthViewModel
import com.example.clicktorun.utils.createSnackBar
import com.example.clicktorun.utils.hideKeyboard
import com.example.clicktorun.utils.startActivityWithAnimation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var binding: FragmentLoginBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
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
            btnLogin.setOnClickListener {
                requireActivity().hideKeyboard()
                authViewModel.logIn()
            }
            linkSignUp.setOnClickListener {
                findNavController().navigate(LoginFragmentDirections.loginToSignUp())
            }
            linkForgetPassword.setOnClickListener {
                findNavController().navigate(LoginFragmentDirections.loginToForgetPassword())
            }
        }
        authViewModel.authState.observe(viewLifecycleOwner) {
            when (it) {
                is AuthViewModel.AuthState.Loading -> binding.apply {
                    progress.visibility = View.VISIBLE
                }
                is AuthViewModel.AuthState.Success -> {
                    authViewModel.getCurrentUserState()
                }
                is AuthViewModel.AuthState.FireStoreUserNotFound -> {
                    try {
                        findNavController().navigate(
                            LoginFragmentDirections.loginToUserDetails()
                        )
                    } catch (e: Exception) {
                        val activity = requireActivity() as AuthActivity
                        activity.navigateToUserDetails()
                    }
                }
                is AuthViewModel.AuthState.FirestoreUserFound -> {
                    Intent(requireContext(), MainActivity::class.java).run {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        requireActivity().startActivityWithAnimation(this)
                        requireActivity().finish()
                    }
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
}