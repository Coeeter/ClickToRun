package com.example.clicktorun.ui.fragments.explore

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clicktorun.R
import com.example.clicktorun.data.models.Post
import com.example.clicktorun.data.models.User
import com.example.clicktorun.databinding.FragmentFollowingBinding
import com.example.clicktorun.ui.adapter.PostAdapter
import com.example.clicktorun.ui.viewmodels.MainViewModel
import com.example.clicktorun.utils.createSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FollowingFragment : Fragment(R.layout.fragment_following), PostAdapter.Listener {
    private lateinit var binding: FragmentFollowingBinding
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFollowingBinding.bind(view)
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
                mainViewModel.getAllPosts().observe(viewLifecycleOwner) {
                    binding.progress.isVisible = false
                    binding.noPosts.isVisible = false
                    if (it.isEmpty() || userList.isEmpty()) {
                        if (binding.recyclerView.adapter != null) {
                            binding.recyclerView.adapter = null
                        }
                        binding.noPosts.isVisible = true
                        return@observe
                    }
                    binding.recyclerView.adapter = adapter.apply {
                        postList = it.filter { post ->
                            followingList.map { user -> user.email }.contains(post.run.email)
                        }
                        notifyDataSetChanged()
                    }
                    binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                }
            }
        }
        mainViewModel.getCurrentUser()
        mainViewModel.getAllPosts()
    }

    override fun setSelectedPost(post: Post) {
        mainViewModel.setSelectedRun(post.run)
        mainViewModel.setSelectedRoute(post.route)
        (parentFragment as ExploreFragment).navigateToDetailsScreen()
    }

    override fun hidePost(runId: String) =
        mainViewModel.removePost(runId)

    override fun followUser(email: String) =
        mainViewModel.addFollow(email)

    override fun unFollowUser(email: String) =
        mainViewModel.unfollowUser(email)

}