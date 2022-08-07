package com.example.clicktorun.ui.fragments.explore

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clicktorun.R
import com.example.clicktorun.data.models.Post
import com.example.clicktorun.databinding.FragmentForYouBinding
import com.example.clicktorun.ui.adapter.PostAdapter
import com.example.clicktorun.ui.viewmodels.MainViewModel
import com.example.clicktorun.utils.createSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForYouFragment : Fragment(R.layout.fragment_for_you), PostAdapter.Listener {
    private lateinit var binding: FragmentForYouBinding
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentForYouBinding.bind(view)
        binding.progress.isVisible = true
        setUpViewModelListeners()
    }

    private fun setUpViewModelListeners() {
        val adapter = PostAdapter(viewLifecycleOwner, this)
        mainViewModel.followingState.observe(viewLifecycleOwner) {
            when (it) {
                is MainViewModel.FollowingState.Loading -> {
                    binding.progress.isVisible = true
                }
                is MainViewModel.FollowingState.Success -> {
                    binding.progress.isVisible = false
                }
                is MainViewModel.FollowingState.Failure -> {
                    binding.progress.isVisible = false
                    binding.root.createSnackBar(
                        message = it.message,
                        okayAction = true
                    ).show()
                }
                else -> {
                }
            }
        }
        mainViewModel.postState.observe(viewLifecycleOwner) {
            when (it) {
                is MainViewModel.PostState.Loading -> {
                    binding.progress.isVisible = true
                }
                is MainViewModel.PostState.Success -> {
                    binding.progress.isVisible = false
                    binding.root.createSnackBar(
                        message = it.message,
                        okayAction = true
                    ).show()
                }
                is MainViewModel.PostState.Failure -> {
                    binding.progress.isVisible = false
                    binding.root.createSnackBar(
                        message = it.message,
                        okayAction = true,
                    ).show()
                }
                else -> {
                }
            }
        }
        mainViewModel.user.observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            mainViewModel.getFollowing(user.email).observe(viewLifecycleOwner) { userList ->
                adapter.followingList = userList
                adapter.notifyDataSetChanged()
            }
        }
        mainViewModel.getAllPosts().observe(viewLifecycleOwner) {
            binding.progress.isVisible = false
            binding.noPosts.isVisible = false
            if (it.isEmpty()) {
                if (binding.recyclerView.adapter != null) {
                    binding.recyclerView.adapter = null
                }
                binding.noPosts.isVisible = true
                return@observe
            }
            binding.recyclerView.adapter = adapter.apply {
                postList = it
                notifyDataSetChanged()
            }
            binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        }
        mainViewModel.getUser()
        mainViewModel.getAllPosts()
    }

    override fun setSelectedPost(post: Post) {
        mainViewModel.setSelectedRun(post.run)
        mainViewModel.setSelectedRoute(post.route)
        (parentFragment as ExploreFragment).navigateToRunDetailsScreen()
    }

    override fun hidePost(runId: String) =
        mainViewModel.removePost(runId)

    override fun followUser(email: String) =
        mainViewModel.addFollow(email)

    override fun unfollowUser(email: String) =
        mainViewModel.unfollowUser(email)

    override fun navigateToUserDetails(email: String) =
        (parentFragment as ExploreFragment).navigateToUserDetailsScreen(email)

}