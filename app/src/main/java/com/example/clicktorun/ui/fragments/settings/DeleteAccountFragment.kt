package com.example.clicktorun.ui.fragments.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.clicktorun.R
import com.example.clicktorun.databinding.FragmentDeleteAccountBinding
import com.example.clicktorun.ui.activities.AuthActivity
import com.example.clicktorun.ui.viewmodels.AuthViewModel
import com.example.clicktorun.ui.viewmodels.MainViewModel
import com.example.clicktorun.utils.ACTION_NAVIGATE_TO_LOGIN
import com.example.clicktorun.utils.createSnackBar
import com.example.clicktorun.utils.hideKeyboard
import com.example.clicktorun.utils.startActivityWithAnimation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteAccountFragment : Fragment(R.layout.fragment_delete_account) {
    private lateinit var binding: FragmentDeleteAccountBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDeleteAccountBinding.bind(view)
        setUpViewModelListeners()
        setUpUiListeners()
    }

    private fun setUpViewModelListeners() {
        authViewModel.authState.observe(viewLifecycleOwner) {
            when (it) {
                is AuthViewModel.AuthState.Loading -> binding.progress.isVisible = true
                is AuthViewModel.AuthState.Success -> {
                    binding.progress.isVisible = false
                    Intent(requireContext(), AuthActivity::class.java).run {
                        action = ACTION_NAVIGATE_TO_LOGIN
                        requireActivity().startActivityWithAnimation(this)
                        requireActivity().finish()
                    }
                }
                is AuthViewModel.AuthState.InvalidPassword -> binding.apply {
                    progress.isVisible = false
                    passwordInput.isErrorEnabled = true
                    passwordInput.error = it.message
                }
                is AuthViewModel.AuthState.FireBaseFailure -> binding.apply {
                    progress.isVisible = false
                    root.createSnackBar(
                        message = it.message ?: "Unknown error has occurred",
                        okayAction = true
                    ).show()
                }
                else -> binding.progress.isVisible = false
            }
        }
    }

    private fun setUpUiListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.passwordInput.editText?.addTextChangedListener {
            binding.passwordInput.isErrorEnabled = false
            authViewModel.password = it.toString()
        }
        binding.btnDeleteAccount.setOnClickListener {
            requireActivity().hideKeyboard()
            mainViewModel.deleteAllPosts()
            mainViewModel.deleteAllFollowsAndFollowing()
            mainViewModel.deleteAllPositions()
            authViewModel.deleteUserAccount()
        }
    }
}