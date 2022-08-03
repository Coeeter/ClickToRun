package com.example.clicktorun.ui.fragments.details

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.clicktorun.R
import com.example.clicktorun.data.models.Position
import com.example.clicktorun.data.models.Run
import com.example.clicktorun.databinding.FragmentRunDetailsBinding
import com.example.clicktorun.ui.viewmodels.MainViewModel
import com.example.clicktorun.utils.getDistance
import com.example.clicktorun.utils.isNightModeEnabled
import com.example.clicktorun.utils.setActionToolbar
import com.example.clicktorun.utils.toTimeString
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunDetailsFragment : Fragment(R.layout.fragment_run_details) {
    private lateinit var binding: FragmentRunDetailsBinding
    private val mainViewModel: MainViewModel by viewModels()
    private val arguments: RunDetailsFragmentArgs by navArgs()
    private var map: GoogleMap? = null
    private var run: Run? = null
    private var isDeleted = false
    private val bounds = LatLngBounds.Builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRunDetailsBinding.bind(view)
        requireActivity().setActionToolbar(binding.toolbar)
        binding.map.onCreate(savedInstanceState)
        setUpUiListeners()
        setUpViewModelListeners()
    }

    private fun setUpUiListeners() {
        requireContext().resources.displayMetrics.apply {
            binding.progress.layoutParams = ViewGroup.LayoutParams(
                widthPixels,
                heightPixels
            )
        }
        binding.progress.isVisible = true
        binding.mainContent.isVisible = false
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.map.getMapAsync {
            map = it
            configureMapType()
        }
        binding.showRouteBtn.setOnClickListener {
            map?.animateCamera(getCameraUpdate())
        }
    }

    private fun getCameraUpdate() = CameraUpdateFactory.newLatLngBounds(
        bounds.build(),
        binding.map.width,
        binding.map.height,
        (binding.map.width * 0.05).toInt(),
    )

    private fun setUpMap(fullRoute: List<List<LatLng>>) {
        for (route in fullRoute) {
            map?.addPolyline(
                PolylineOptions().apply {
                    width(8f)
                    color(requireContext().getColor(R.color.primary))
                    addAll(route)
                }
            )
            route.forEach { pos -> bounds.include(pos) }
        }
        map?.moveCamera(getCameraUpdate())
    }

    private fun setUpViewModelListeners() {
        mainViewModel.user.observe(viewLifecycleOwner) {
            it ?: return@observe
            mainViewModel.getRunList(it.email).observe(viewLifecycleOwner) { runList ->
                binding.progress.isVisible = false
                binding.mainContent.isVisible = true
                if (checkIfDeleted()) return@observe
                run = runList[arguments.index]
                binding.distanceRan.text = if (run!!.distanceRanInMetres < 1000)
                    "${run!!.distanceRanInMetres}m"
                else
                    "${run!!.distanceRanInMetres / 1000}km"
                binding.timeTaken.text = run!!.timeTakenInMilliseconds.toTimeString()
                binding.averageSpeed.text = "${run!!.averageSpeedInKilometersPerHour}km/h"
                binding.caloriesBurnt.text = "${run!!.caloriesBurnt}kcal"
                mainViewModel.getPositionList(run!!)
            }
        }
        mainViewModel.runRoute.observe(viewLifecycleOwner) {
            run ?: return@observe
            setUpMap(it.map { list ->
                list.map { pos -> pos.getLatLng() }
            })
            val positionList = mutableListOf<Position>()
            for (positions in it) {
                positionList.addAll(positions)
            }
            binding.distanceOverTimeGraph.apply {
                val values = mutableListOf<Entry>()
                for (i in positionList.indices) {
                    val position = positionList[i]
                    val latLngList = positionList.subList(0, i + 1).map { pos ->
                        pos.getLatLng()
                    }
                    val distanceRan = listOf(latLngList)
                        .getDistance()
                        .toFloat()
                    values.add(
                        Entry(
                            (position.timeReachedPosition - (run!!.timeEnded - run!!.timeTakenInMilliseconds)).toFloat(),
                            distanceRan
                        )
                    )
                }
                val lineDataSet = createLineDataSet(values, "Distance ran over time")
                data = LineData(listOf(lineDataSet))
                setUpGraph(this)
                invalidate()
            }
            binding.averageSpeedOverTimeGraph.apply {
                val values = mutableListOf<Entry>()
                for (i in positionList.indices) {
                    val position = positionList[i]
                    values.add(
                        Entry(
                            (position.timeReachedPosition - (run!!.timeEnded - run!!.timeTakenInMilliseconds)).toFloat(),
                            (position.speedInMetresPerSecond * 3.6).toFloat()
                        )
                    )
                }
                val lineDataSet = createLineDataSet(values, "Average speed over time")
                data = LineData(listOf(lineDataSet))
                setUpGraph(this)
                invalidate()
            }
            binding.caloriesBurntOverTimeGraph.apply {
                val values = mutableListOf<Entry>()
                for (i in positionList.indices) {
                    val position = positionList[i]
                    values.add(
                        Entry(
                            (position.timeReachedPosition - (run!!.timeEnded - run!!.timeTakenInMilliseconds)).toFloat(),
                            position.caloriesBurnt.toFloat()
                        )
                    )
                }
                val lineDataSet = createLineDataSet(values, "Calories burnt over time")
                data = LineData(listOf(lineDataSet))
                setUpGraph(this)
                invalidate()
            }
        }
        mainViewModel.getCurrentUser()
    }

    private fun createLineDataSet(values: List<Entry>, label: String) =
        LineDataSet(values, label).apply {
            setDrawValues(false)
            setDrawCircles(false)
            colors = listOf(requireContext().getColor(R.color.primary))
            lineWidth = 2f
        }

    private fun setUpGraph(lineChart: LineChart) {
        lineChart.apply {
            setTouchEnabled(false)
            xAxis.isEnabled = false
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            legend.isEnabled = false
            description = null
        }
    }

    private fun checkIfDeleted(): Boolean {
        if (isDeleted) findNavController().popBackStack()
        return isDeleted
    }

    private fun configureMapType() {
        if (requireActivity().isNightModeEnabled()) {
            map?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_night_style,
                )
            )
            return
        }
        map?.setMapStyle(MapStyleOptions("[]"));
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.miDelete -> {
                binding.progress.isVisible = true
                mainViewModel.deleteRun(listOf(run!!))
                mainViewModel.deletePositionList(listOf(run!!))
                isDeleted = true
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()
        binding.map.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.map.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.map.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.map.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.map.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }
}