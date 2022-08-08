package com.example.clicktorun.ui.fragments.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.clicktorun.R
import com.example.clicktorun.databinding.FragmentSplashScreenBinding
import com.example.clicktorun.ui.activities.MainActivity
import com.example.clicktorun.ui.viewmodels.AuthViewModel
import com.example.clicktorun.utils.ACTION_NAVIGATE_TO_LOGIN
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashScreenFragment : Fragment(R.layout.fragment_splash_screen),
    Animation.AnimationListener {
    private lateinit var binding: FragmentSplashScreenBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSplashScreenBinding.bind(view)
        if (requireActivity().intent.action == ACTION_NAVIGATE_TO_LOGIN) return
        setUpViewModelListeners()
        startAnimation()
    }

    private fun startAnimation() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000L)
            val animation = AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.slide_out_right
            )
            animation.setAnimationListener(this@SplashScreenFragment)
            binding.brand.startAnimation(animation)
            delay(150L)
            binding.appName.visibility = View.VISIBLE
            delay(300L)
            if (authViewModel.currentUser == null) {
                findNavController().navigate(SplashScreenFragmentDirections.splashToLogin())
                return@launch
            }
            authViewModel.getCurrentUserState()
        }
    }

    private fun setUpViewModelListeners() {
        authViewModel.authState.observe(viewLifecycleOwner) {
            when (it) {
                is AuthViewModel.AuthState.FirestoreUserFound -> {
                    Intent(requireContext(), MainActivity::class.java).run {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(this)
                        requireActivity().finish()
                        requireActivity().overridePendingTransition(0, 0)
                    }
                }
                is AuthViewModel.AuthState.FireStoreUserNotFound -> {
                    findNavController().navigate(
                        SplashScreenFragmentDirections.splashToUserDetails()
                    )
                }
                else -> {
                }
            }
        }
    }

    override fun onAnimationStart(animation: Animation?) {}
    override fun onAnimationRepeat(animation: Animation?) {}
    override fun onAnimationEnd(animation: Animation?) {
        binding.brand.visibility = View.GONE
    }

}