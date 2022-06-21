package com.example.clicktorun.ui.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.clicktorun.R
import com.example.clicktorun.databinding.FragmentInsightsBinding

class InsightsFragment : Fragment(R.layout.fragment_insights) {
    private lateinit var binding: FragmentInsightsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInsightsBinding.bind(view)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        view?.let { onViewCreated(it, null) }
    }
}