package com.example.clicktorun.ui.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.clicktorun.R
import com.example.clicktorun.databinding.FragmentHomeBinding
import com.example.clicktorun.ui.adapter.RunAdapter
import com.example.clicktorun.ui.viewmodels.TrackingViewModel
import com.example.clicktorun.utils.isNightModeEnabled
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private val trackingViewModel: TrackingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        binding.btnAddRun.background = AppCompatResources.getDrawable(
            requireContext(),
            R.drawable.custom_fab_light_mode_background
        )
        if (requireContext().isNightModeEnabled()) {
            binding.btnAddRun.background = AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.custom_fab_dark_mode_background
            )
        }
        binding.btnAddRun.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.homeToTracking())
        }
        trackingViewModel.user.observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            trackingViewModel.getRunList(user.email).observe(viewLifecycleOwner) {
                if (it.isEmpty()) return@observe binding.noRunsView.setVisibility(View.VISIBLE)
                binding.noRunsView.visibility = View.GONE
                binding.recyclerView.adapter = RunAdapter().apply { setRunList(it) }
            }
        }
        trackingViewModel.getCurrentUser()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        view?.let { onViewCreated(it, null) }
    }
}