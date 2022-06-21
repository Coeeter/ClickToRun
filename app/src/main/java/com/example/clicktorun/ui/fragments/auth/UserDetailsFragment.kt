package com.example.clicktorun.ui.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.clicktorun.R
import com.example.clicktorun.databinding.FragmentUserDetailsBinding
import com.example.clicktorun.ui.activities.MainActivity
import com.example.clicktorun.ui.viewmodels.AuthViewModel
import com.example.clicktorun.utils.createSnackBar
import com.example.clicktorun.utils.hideKeyboard
import com.example.clicktorun.utils.startActivityWithAnimation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserDetailsFragment : Fragment(R.layout.fragment_user_details) {
    private lateinit var binding: FragmentUserDetailsBinding
    val authViewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserDetailsBinding.bind(view)
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
            requireActivity().hideKeyboard()
            authViewModel.insertUser()
        }
    }

    private fun setUpViewModelListener() {
        authViewModel.authState.observe(viewLifecycleOwner) {
            when (it) {
                is AuthViewModel.AuthState.Loading -> binding.apply {
                    progress.visibility = View.VISIBLE
                }
                is AuthViewModel.AuthState.Success -> binding.apply {
                    hideLoading()
                    Intent(requireContext(), MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        requireActivity().startActivityWithAnimation(this)
                        requireActivity().finish()
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
                    binding.root.createSnackBar(
                        message = it.message ?: "Unknown error has occurred",
                        okayAction = true
                    ).show()
                }
                else -> binding.apply {
                    hideLoading()
                }
            }
        }
    }

    private fun hideLoading() = binding.apply {
        progress.visibility = View.GONE
    }
}