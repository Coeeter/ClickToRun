package com.example.clicktorun.ui.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.clicktorun.R
import com.example.clicktorun.databinding.FragmentInsightsBinding
import com.example.clicktorun.ui.viewmodels.MainViewModel
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InsightsFragment : Fragment(R.layout.fragment_insights) {
    private lateinit var binding: FragmentInsightsBinding
    private val mainViewModel: MainViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInsightsBinding.bind(view)

        mainViewModel.user.observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            mainViewModel.getRunList(user.email).observe(viewLifecycleOwner) {
                val entries = it.indices.map { i ->
                    BarEntry(i.toFloat(), it[i].averageSpeedInKilometersPerHour!!.toFloat())
                }.toMutableList()
                binding.chart.apply {
                    data = BarData(
                        BarDataSet(entries, "Average Speed over time").apply {
                            valueTextColor = getColor(R.color.primary)
                            color = getColor(R.color.secondary)
                            valueTextSize = 30f

                        }
                    )
                    invalidate()
                }
            }
        }
        mainViewModel.getCurrentUser()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        view?.let { onViewCreated(it, null) }
    }
}