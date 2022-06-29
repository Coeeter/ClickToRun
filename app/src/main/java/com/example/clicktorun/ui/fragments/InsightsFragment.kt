package com.example.clicktorun.ui.fragments

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.clicktorun.R
import com.example.clicktorun.data.models.Run
import com.example.clicktorun.databinding.FragmentInsightsBinding
import com.example.clicktorun.ui.viewmodels.TrackingViewModel
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class InsightsFragment : Fragment(R.layout.fragment_insights) {
    private lateinit var binding: FragmentInsightsBinding
    private val trackingViewModel: TrackingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentInsightsBinding.bind(view)

        trackingViewModel.user.observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            trackingViewModel.getRunList(user.email).observe(viewLifecycleOwner) {
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
        trackingViewModel.getCurrentUser()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        view?.let { onViewCreated(it, null) }
    }
}