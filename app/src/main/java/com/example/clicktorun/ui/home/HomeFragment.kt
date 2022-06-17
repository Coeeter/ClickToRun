package com.example.clicktorun.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        binding.btnAddRun.setOnClickListener {
            findNavController().navigate(R.id.action_miHome_to_trackingFragment)
        }
        trackingViewModel.runList.observe(viewLifecycleOwner) {
            binding.recyclerView.adapter = RunAdapter().apply { setRunList(it) }
        }
    }
}