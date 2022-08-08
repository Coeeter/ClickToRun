package com.example.clicktorun.ui.fragments.settings

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.clicktorun.R
import com.example.clicktorun.data.models.User
import com.example.clicktorun.databinding.FragmentSettingsBinding
import com.example.clicktorun.services.RunService
import com.example.clicktorun.ui.activities.AuthActivity
import com.example.clicktorun.ui.viewmodels.AuthViewModel
import com.example.clicktorun.ui.viewmodels.MainViewModel
import com.example.clicktorun.utils.ACTION_NAVIGATE_TO_LOGIN
import com.example.clicktorun.utils.loadImage
import com.example.clicktorun.utils.startActivityWithAnimation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private lateinit var binding: FragmentSettingsBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private var user: User? = null
    private var isLoading = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)
        setUpObservers()
        setUpListeners()
    }

    private fun setUpObservers() {
        mainViewModel.user.observe(viewLifecycleOwner) {
            user = it
            it ?: return@observe
            binding.username.text = user!!.username
            user!!.profileImage.loadImage(
                viewLifecycleOwner,
                binding.profileImage,
                binding.imageProgress,
                true,
            ) { isLoading = false }
        }
        mainViewModel.getUser()
    }

    private fun setUpListeners() {
        binding.imageProgress.isVisible = true
        binding.btnSignOut.setOnClickListener {
            if (RunService.isTracking.value == true) return@setOnClickListener
            authViewModel.signOut()
            Intent(requireContext(), AuthActivity::class.java).run {
                action = ACTION_NAVIGATE_TO_LOGIN
                requireActivity().startActivityWithAnimation(this)
                requireActivity().finish()
            }
        }
        binding.editAccountBtn.setOnClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionMiSettingsToEditAccountFragment()
            )
        }
        binding.deleteAccountBtn.setOnClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionMiSettingsToDeleteAccountFragment()
            )
        }
        binding.details.setOnClickListener {
            if (isLoading) return@setOnClickListener
            findNavController().navigate(
                SettingsFragmentDirections.actionMiSettingsToProfileFragment(email = authViewModel.currentUser!!.email!!)
            )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        view?.let { onViewCreated(it, null) }
    }
}