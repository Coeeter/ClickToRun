package com.example.clicktorun.ui.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.clicktorun.R
import com.example.clicktorun.databinding.FragmentRunsBinding
import com.example.clicktorun.ui.adapter.RunAdapter
import com.example.clicktorun.ui.viewmodels.TrackingViewModel
import com.example.clicktorun.utils.isNightModeEnabled
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class YourRunsFragment : Fragment(R.layout.fragment_runs) {
    private lateinit var binding: FragmentRunsBinding
    private val trackingViewModel: TrackingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRunsBinding.bind(view)
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
            findNavController().navigate(YourRunsFragmentDirections.runsToTracking())
        }
        trackingViewModel.user.observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            trackingViewModel.getRunList(user.email).observe(viewLifecycleOwner) {
                if (it.isEmpty()) return@observe binding.noRunsView.setVisibility(View.VISIBLE)
                binding.noRunsView.visibility = View.GONE
                binding.recyclerView.adapter = RunAdapter(it)
            }
        }
        trackingViewModel.getCurrentUser()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        view?.let { onViewCreated(it, null) }
    }
}