package com.example.clicktorun.ui.fragments.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clicktorun.R
import com.example.clicktorun.databinding.FragmentProfileFollowingBinding
import com.example.clicktorun.ui.adapter.UserAdapter
import com.example.clicktorun.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFollowingFragment : Fragment(R.layout.fragment_profile_following) {
    private lateinit var binding: FragmentProfileFollowingBinding
    private lateinit var email: String
    private val mainViewModel: MainViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileFollowingBinding.bind(view)
        email = (parentFragment as ProfileDetailsFragment).arguments.email
        binding.progress.isVisible = true
        setUpViewModelListeners()
    }

    private fun setUpViewModelListeners() {
        mainViewModel.getFollowing(email).observe(viewLifecycleOwner) { following ->
            binding.progress.isVisible = false
            binding.noFollowing.isVisible = false
            if (following.isEmpty()) {
                binding.noFollowing.isVisible = true
                return@observe
            }
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerView.adapter = UserAdapter(following, viewLifecycleOwner) {
                (parentFragment as ProfileDetailsFragment).navigateToProfilePage(it)
            }
        }
    }
}