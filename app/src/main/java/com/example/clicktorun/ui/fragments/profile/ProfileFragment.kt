package com.example.clicktorun.ui.fragments.profile

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clicktorun.R
import com.example.clicktorun.data.models.Post
import com.example.clicktorun.databinding.FragmentProfileBinding
import com.example.clicktorun.ui.adapter.PostAdapter
import com.example.clicktorun.ui.viewmodels.AuthViewModel
import com.example.clicktorun.ui.viewmodels.MainViewModel
import com.example.clicktorun.utils.createSnackBar
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile), PostAdapter.Listener {
    private lateinit var binding: FragmentProfileBinding
    private val mainViewModel: MainViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val arguments: ProfileFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)
        setUpUiListeners()
        setUpViewModelListeners()
    }

    private fun setUpUiListeners() {
        binding.imageProgress.isVisible = true
        binding.mainProgress.isVisible = true
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.followingCount.setOnClickListener {
            navigateToFollowingFragment()
        }
        binding.followingLabel.setOnClickListener {
            navigateToFollowingFragment()
        }
        binding.followersCount.setOnClickListener {
            navigateToFollowersFragment()
        }
        binding.followersLabel.setOnClickListener {
            navigateToFollowersFragment()
        }
    }

    private fun navigateToFollowingFragment() {
        findNavController().navigate(
            ProfileFragmentDirections.actionProfileFragmentToProfileDetailsFragment(
                email = arguments.email,
                position = 1
            )
        )
    }

    private fun navigateToFollowersFragment() {
        findNavController().navigate(
            ProfileFragmentDirections.actionProfileFragmentToProfileDetailsFragment(
                email = arguments.email,
                position = 0
            )
        )
    }

    private fun setUpViewModelListeners() {
        mainViewModel.getFollowing(arguments.email).observe(viewLifecycleOwner) { userList ->
            binding.followingCount.text = userList.size.toString()
        }
        mainViewModel.getFollowers(arguments.email).observe(viewLifecycleOwner) { userList ->
            binding.followersCount.text = userList.size.toString()
            if (arguments.email == authViewModel.currentUser!!.email) {
                binding.mainBtn.text = "Edit Profile"
                binding.mainBtn.setOnClickListener {
                    findNavController().navigate(
                        ProfileFragmentDirections.actionProfileFragmentToEditAccountFragment()
                    )
                }
                return@observe
            }
            if (userList.map { it.email }.contains(authViewModel.currentUser!!.email)) {
                binding.mainBtn.text = "Unfollow"
                binding.mainBtn.setOnClickListener {
                    mainViewModel.unfollowUser(arguments.email)
                }
                return@observe
            }
            binding.mainBtn.text = "Follow"
            binding.mainBtn.setOnClickListener {
                mainViewModel.addFollow(arguments.email)
            }
        }
        mainViewModel.getAllPostsOfUser(arguments.email).observe(viewLifecycleOwner) { postList ->
            binding.noPosts.isVisible = false
            if (postList.isEmpty()) {
                binding.noPosts.isVisible = true
                return@observe
            }
            binding.recyclerView.adapter = PostAdapter(viewLifecycleOwner, this).apply {
                this.postList = postList
                isProfileDetail = true
                notifyDataSetChanged()
            }
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.mainProgress.isVisible = false
        }
        mainViewModel.followingState.observe(viewLifecycleOwner) {
            when (it) {
                is MainViewModel.FollowingState.Loading -> {
                    binding.mainProgress.isVisible = true
                }
                is MainViewModel.FollowingState.Success -> {
                    binding.mainProgress.isVisible = false
                }
                is MainViewModel.FollowingState.Failure -> {
                    binding.mainProgress.isVisible = false
                    binding.root.createSnackBar(
                        message = it.message,
                        okayAction = true,
                    ).show()
                }
                else -> {}
            }
        }
        mainViewModel.user.observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            binding.imageProgress.isVisible = false
            binding.profileImage.setImageResource(R.drawable.ic_baseline_person_24)
            if (user.profileImage != null) {
                binding.imageProgress.isVisible = true
                user.profileImage.observe(viewLifecycleOwner) {
                    Picasso.with(requireContext())
                        .load(it)
                        .into(binding.profileImage, object : Callback {
                            override fun onSuccess() {
                                binding.imageProgress.isVisible = false
                            }

                            override fun onError() {
                                binding.imageProgress.isVisible = false
                            }
                        })
                }
            }
            binding.username.text = user.username
            binding.toolbar.title = "${user.username}'s profile"
            binding.mainProgress.isVisible = false
        }
        mainViewModel.getUser(arguments.email)
    }

    override fun setSelectedPost(post: Post) {
        mainViewModel.setSelectedRun(post.run)
        mainViewModel.setSelectedRoute(post.route)
        findNavController().navigate(
            ProfileFragmentDirections.actionProfileFragmentToRunDetailsFragment()
        )
    }

    override fun hidePost(runId: String) {}
    override fun followUser(email: String) {}
    override fun unfollowUser(email: String) {}
    override fun navigateToUserDetails(email: String) {}
}