package com.example.clicktorun.ui.fragments.explore

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.clicktorun.R
import com.example.clicktorun.databinding.FragmentExploreBinding
import com.example.clicktorun.utils.ViewPager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExploreFragment : Fragment(R.layout.fragment_explore) {
    private lateinit var binding: FragmentExploreBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentExploreBinding.bind(view)
        setUpUi()
    }

    private fun setUpUi() {
        binding.viewPager.adapter = ViewPager(childFragmentManager).apply {
            addFragment(
                ForYouFragment(),
                "For you"
            )
            addFragment(
                FollowingFragment(),
                "Following"
            )
        }
        binding.tabs.setupWithViewPager(binding.viewPager)
    }

    fun navigateToRunDetailsScreen() {
        findNavController().navigate(
            ExploreFragmentDirections.actionMiExploreToRunDetailsFragment()
        )
    }

    fun navigateToUserDetailsScreen(email: String) {
        findNavController().navigate(
            ExploreFragmentDirections.actionMiExploreToProfileFragment(email = email)
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        view?.let { onViewCreated(it, null) }
    }

}