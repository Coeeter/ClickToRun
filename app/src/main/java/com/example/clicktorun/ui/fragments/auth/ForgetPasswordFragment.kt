package com.example.clicktorun.ui.fragments.auth

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.clicktorun.R
import com.example.clicktorun.databinding.FragmentForgetPasswordBinding
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
class ForgetPasswordFragment : Fragment(R.layout.fragment_forget_password) {
    private lateinit var binding: FragmentForgetPasswordBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentForgetPasswordBinding.bind(view)
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
            toolbar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            emailInput.editText?.addTextChangedListener {
                emailInput.isErrorEnabled = false
                authViewModel.email = it.toString()
            }
            btnSendEmail.setOnClickListener {
                requireActivity().hideKeyboard()
                authViewModel.sendPasswordResetLinkToEmail()
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
                is AuthViewModel.AuthState.InvalidEmail -> binding.apply {
                    progress.visibility = View.GONE
                    emailInput.apply {
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
            message = "Check your email for further instructions on how to reset your password",
            okayAction = true
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
                findNavController().popBackStack()
            }
        }).show()
    }
}