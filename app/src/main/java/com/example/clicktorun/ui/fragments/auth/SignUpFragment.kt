package com.example.clicktorun.ui.fragments.auth

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.clicktorun.R
import com.example.clicktorun.databinding.FragmentSignUpBinding
import com.example.clicktorun.ui.viewmodels.AuthViewModel
import com.example.clicktorun.utils.createSnackBar
import com.example.clicktorun.utils.hideKeyboard
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFragment : Fragment(R.layout.fragment_sign_up) {
    private lateinit var binding: FragmentSignUpBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignUpBinding.bind(view)
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
            toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            linkLogin.setOnClickListener {
                findNavController().popBackStack()
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
            btnSignUp.setOnClickListener {
                requireActivity().hideKeyboard()
                authViewModel.signUp()
            }
        }
        authViewModel.authState.observe(viewLifecycleOwner) {
            when (it) {
                is AuthViewModel.AuthState.Loading -> binding.apply {
                    progress.visibility = View.VISIBLE
                }
                is AuthViewModel.AuthState.Success -> binding.apply {
                    progress.visibility = View.GONE
                    createSnackbar()
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
                is AuthViewModel.AuthState.InvalidConfirmPassword -> binding.apply {
                    progress.visibility = View.GONE
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
                }
            }
        }
    }

    private fun createSnackbar() {
        binding.root.createSnackBar(
            "Successfully created account!",
        ).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
            override fun onShown(transientBottomBar: Snackbar?) {
                super.onShown(transientBottomBar)
                CoroutineScope(Dispatchers.Main).launch {
                    delay(3000L)
                    findNavController().popBackStack()
                }
            }

            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
            }
        }).show()
    }
}