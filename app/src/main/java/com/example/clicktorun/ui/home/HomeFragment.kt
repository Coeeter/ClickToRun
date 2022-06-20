package com.example.clicktorun.ui.home

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.clicktorun.R
import com.example.clicktorun.databinding.FragmentHomeBinding
import com.example.clicktorun.ui.adapter.RunAdapter
import com.example.clicktorun.ui.tracking.TrackingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private val trackingViewModel: TrackingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        binding.toolbar.title = "Your Runs"
        binding.btnAddRun.setOnClickListener {
            findNavController().navigate(R.id.action_miHome_to_trackingFragment)
        }
        trackingViewModel.user.observe(viewLifecycleOwner) {
            it ?: return@observe
            trackingViewModel.getRunList(it.email).observe(viewLifecycleOwner) {
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