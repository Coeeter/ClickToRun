package com.example.clicktorun.ui.fragments.profile

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clicktorun.R
import com.example.clicktorun.databinding.FragmentProfileFollowersBinding
import com.example.clicktorun.ui.adapter.UserAdapter
import com.example.clicktorun.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFollowersFragment : Fragment(R.layout.fragment_profile_followers) {
    private lateinit var binding: FragmentProfileFollowersBinding
    private lateinit var email: String
    private val mainViewModel: MainViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileFollowersBinding.bind(view)
        email = (parentFragment as ProfileDetailsFragment).arguments.email
        binding.progress.isVisible = true
        setUpViewModelListeners()
    }

    private fun setUpViewModelListeners() {
        mainViewModel.getFollowers(email).observe(viewLifecycleOwner) { followers ->
            binding.progress.isVisible = false
            binding.noFollowers.isVisible = false
            if (followers.isEmpty()) {
                binding.noFollowers.isVisible = true
                return@observe
            }
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.adapter = UserAdapter(followers, viewLifecycleOwner) {
                (parentFragment as ProfileDetailsFragment).navigateToProfilePage(it)
            }
        }
    }
}