package com.example.clicktorun.ui.fragments.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.clicktorun.R
import com.example.clicktorun.databinding.FragmentProfileDetailsBinding
import com.example.clicktorun.ui.viewmodels.MainViewModel
import com.example.clicktorun.utils.ViewPager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileDetailsFragment : Fragment(R.layout.fragment_profile_details) {
    private lateinit var binding: FragmentProfileDetailsBinding
    private val mainViewModel: MainViewModel by viewModels()
    val arguments: ProfileDetailsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileDetailsBinding.bind(view)
        setUpUi()
        setUpViewModelListeners()
    }

    private fun setUpViewModelListeners() {
        mainViewModel.user.observe(viewLifecycleOwner) {
            it ?: return@observe
            binding.toolbar.title = "${it.username}'s profile"
        }
        mainViewModel.getUser(arguments.email)
    }

    private fun setUpUi() {
        binding.viewPager.adapter = ViewPager(childFragmentManager).apply {
            addFragment(
                ProfileFollowersFragment(),
                "Followers"
            )
            addFragment(
                ProfileFollowingFragment(),
                "Following"
            )
        }
        binding.tabs.setupWithViewPager(binding.viewPager)
        binding.viewPager.setCurrentItem(arguments.position, false)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    fun navigateToProfilePage(email: String) {
        findNavController().navigate(
            ProfileDetailsFragmentDirections.actionProfileDetailsFragmentToProfileFragment(email)
        )
    }
}