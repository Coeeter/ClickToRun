package com.example.clicktorun.ui.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.SweepGradient
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.clicktorun.R
import com.example.clicktorun.data.models.Run
import com.example.clicktorun.databinding.FragmentInsightsBinding
import com.example.clicktorun.ui.viewmodels.MainViewModel
import com.example.clicktorun.utils.toTimeString
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round


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
                binding.progress.visibility = View.GONE
                if (it.isEmpty()) {
                    return@observe binding.nothingDisplay.setVisibility(View.VISIBLE)
                }
                binding.mainBody.visibility = View.VISIBLE
                populateMainBodyDataValues(it)
                populateGraphs(it)
            }
        }
        mainViewModel.getCurrentUser()
    }

    private fun populateGraphs(list: List<Run>) {
        val avgSpeedEntries = mutableListOf<BarEntry>()
        val distanceEntries = mutableListOf<BarEntry>()
        val timeTakenEntries = mutableListOf<BarEntry>()
        for (i in list.indices) {
            val run = list[i]
            avgSpeedEntries.add(
                BarEntry(i.toFloat(), run.averageSpeedInKilometersPerHour!!.toFloat())
            )
            distanceEntries.add(
                BarEntry(i.toFloat(), run.distanceRanInMetres.toFloat())
            )
            timeTakenEntries.add(
                BarEntry(i.toFloat(), run.timeTakenInMilliseconds.toFloat())
            )
        }
        setUpGraph(binding.distanceOverRunGraph, distanceEntries)
        setUpGraph(binding.timeTakenGraph, timeTakenEntries)
        setUpGraph(binding.avgSpeedGraph, avgSpeedEntries)
    }

    private fun getDataSet(entries: List<BarEntry>) = BarDataSet(entries, "").apply {
        setDrawValues(false)
        listOf(
            requireContext().getColor(R.color.primary),
            requireContext().getColor(R.color.secondary)
        ).also { colors = it }
    }

    private fun setUpGraph(graph: BarChart, entries: List<BarEntry>) {
        graph.apply {
            setTouchEnabled(false)
            xAxis.isEnabled = false
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            legend.isEnabled = false
            description = null
            data = BarData(getDataSet(entries))
            invalidate()
        }
    }

    private fun populateMainBodyDataValues(runList: List<Run>) {
        binding.numberOfRunsValue.text = runList.size.toString()
        var totalDistance = 0
        var totalTimeTaken = 0L
        for (run in runList) {
            totalDistance += run.distanceRanInMetres
            totalTimeTaken += run.timeTakenInMilliseconds
        }
        binding.totalDistanceValue.text = if (totalDistance < 1000) "${totalDistance}m"
        else "${totalDistance / 1000.0}km"
        val timeTakenList = totalTimeTaken.toTimeString().split(":")
        var hours = timeTakenList[0].toInt().toString() + "hr "
        var minutes = timeTakenList[1].toInt().toString() + "min "
        val seconds = timeTakenList[2].toInt().toString() + "s"
        if (hours == "0hr ") hours = ""
        if (minutes == "0min ") minutes = ""
        binding.totalTimeValue.text = "$hours$minutes$seconds"
        binding.totalAvgSpeedValue.text = (round(
            ((totalDistance) / (totalTimeTaken / 1000) * 360).toDouble()
        ) / 100).toString() + "km/h"
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        view?.let { onViewCreated(it, null) }
    }
}