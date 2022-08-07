package com.example.clicktorun.ui.fragments.details

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.clicktorun.R
import com.example.clicktorun.data.models.Position
import com.example.clicktorun.data.models.Run
import com.example.clicktorun.databinding.FragmentRunDetailsBinding
import com.example.clicktorun.ui.viewmodels.AuthViewModel
import com.example.clicktorun.ui.viewmodels.MainViewModel
import com.example.clicktorun.utils.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.max
import kotlin.math.min

@AndroidEntryPoint
class RunDetailsFragment : Fragment(R.layout.fragment_run_details) {
    private lateinit var binding: FragmentRunDetailsBinding
    private val mainViewModel: MainViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private var map: GoogleMap? = null
    private var run: Run? = null
    private var isPosted = false
    private var route: List<List<Position>>? = null
    private var isDeleted = false
    private var deleteMenuItem: MenuItem? = null
    private var shareMenuItem: MenuItem? = null
    private var hideMenuItem: MenuItem? = null
    private var bounds: LatLngBounds? = null

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
        deleteMenuItem?.isVisible = false
        shareMenuItem?.isVisible = false
        hideMenuItem?.isVisible = false
        binding.fullProgress.isVisible = true
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.map.getMapAsync {
            map = it
            configureMapType()
            setUpMap()
        }
        binding.showRouteBtn.setOnClickListener {
            getCameraUpdate()?.let { map?.animateCamera(it) }
        }
    }

    private fun getCameraUpdate(): CameraUpdate? {
        if (bounds == null) return null
        return CameraUpdateFactory.newLatLngBounds(
            bounds!!,
            binding.map.width,
            binding.map.height,
            (binding.map.width * 0.05).toInt(),
        )
    }

    private fun setUpMap() {
        if (route == null) return
        map?.clear()
        val fullRoute = route!!.convertToLatLng()
        var south = fullRoute[0][0].latitude
        var north = fullRoute[0][0].latitude
        var east = fullRoute[0][0].longitude
        var west = fullRoute[0][0].longitude
        for (latLngList in fullRoute) {
            map?.addPolyline(
                PolylineOptions().apply {
                    width(8f)
                    color(requireContext().getColor(R.color.primary))
                    addAll(latLngList)
                }
            )
            latLngList.forEach { latlng ->
                south = min(south, latlng.latitude)
                north = max(north, latlng.latitude)
                west = min(west, latlng.longitude)
                east = max(east, latlng.longitude)
            }
        }
        bounds = LatLngBounds(
            LatLng(south, west),
            LatLng(north, east)
        )
        getCameraUpdate()?.let { map?.moveCamera(it) }
    }

    private fun setUpViewModelListeners() {
        mainViewModel.getAllPosts().observe(viewLifecycleOwner) { postList ->
            if (authViewModel.currentUser!!.email!! != run!!.email) {
                shareMenuItem?.isVisible = false
                hideMenuItem?.isVisible = false
                deleteMenuItem?.isVisible = false
                return@observe
            }
            isPosted = postList.map { it.run.id }.contains(run?.id)
            shareMenuItem?.isVisible = !isPosted
            hideMenuItem?.isVisible = isPosted
            deleteMenuItem?.isVisible = true
        }
        mainViewModel.selectedRun.observe(viewLifecycleOwner) {
            run = it
            bindData(run!!)
            mainViewModel.getPositionList(run!!)
        }
        mainViewModel.selectedRoute.observe(viewLifecycleOwner) {
            route = it
            setUpMap()
            setUpGraph()
            binding.fullProgress.isVisible = false
        }
        mainViewModel.runRoute.observe(viewLifecycleOwner) {
            if (it == null || it.isEmpty()) return@observe
            route = it
            setUpMap()
            setUpGraph()
            binding.fullProgress.isVisible = false
        }
        mainViewModel.postState.observe(viewLifecycleOwner) {
            when (it) {
                is MainViewModel.PostState.Loading -> {
                    binding.fullProgress.isVisible = true
                }
                is MainViewModel.PostState.Success -> {
                    binding.fullProgress.isVisible = false
                    var message = it.message
                    if (isDeleted) message = "Deleted run successfully"
                    binding.root
                        .createSnackBar(
                            message,
                            okayAction = true
                        ).show()
                    if (isDeleted) {
                        findNavController().popBackStack()
                        return@observe
                    }
                    shareMenuItem?.isVisible = !it.isInserted
                    hideMenuItem?.isVisible = it.isInserted
                }
                is MainViewModel.PostState.Failure -> {
                    binding.fullProgress.isVisible = false
                    binding.root.createSnackBar(it.message, okayAction = true).show()
                }
                else -> {
                }
            }
        }
    }

    private fun setUpGraph() {
        if (route == null) return
        val positionList = route!!.flatten()
        run?.let {
            val graphList = binding.run {
                listOf(distanceOverTimeGraph, averageSpeedOverTimeGraph, caloriesBurntOverTimeGraph)
            }
            for (i in graphList.indices) setUpGraphValues(
                graphList[i],
                it,
                positionList,
                MainViewModel.GraphType.values()[i]
            )
        }
    }

    private fun setUpGraphValues(
        graph: LineChart,
        run: Run,
        positionList: List<Position>,
        graphType: MainViewModel.GraphType,
    ) {
        graph.apply {
            val lineDataSet = LineDataSet(
                mainViewModel.getEntryList(
                    run,
                    positionList,
                    graphType
                ),
                ""
            ).apply {
                setDrawValues(false)
                setDrawCircles(false)
                colors = listOf(requireContext().getColor(R.color.primary))
                lineWidth = 2f
            }
            data = LineData(listOf(lineDataSet))
            setTouchEnabled(false)
            for (axis in listOf(xAxis, axisLeft, axisRight, legend))
                axis.isEnabled = false
            description = null
            invalidate()
        }
    }

    private fun bindData(run: Run) {
        binding.distanceRan.text = if (run.distanceRanInMetres < 1000)
            "${run.distanceRanInMetres}m"
        else
            "${run.distanceRanInMetres / 1000}km"
        binding.timeTaken.text = run.timeTakenInMilliseconds.toTimeString()
        binding.averageSpeed.text = "${run.averageSpeedInKilometersPerHour}km/h"
        binding.caloriesBurnt.text = "${run.caloriesBurnt}kcal"
        val timeStartedInMilliseconds = run.timeEnded - run.timeTakenInMilliseconds
        binding.dateRan.text = timeStartedInMilliseconds.getDate()
        binding.timeRan.text = timeStartedInMilliseconds.getTime()
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
        map?.setMapStyle(MapStyleOptions("[]"))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu, menu)
        deleteMenuItem = menu.findItem(R.id.miDelete).apply {
            isVisible = false
        }
        shareMenuItem = menu.findItem(R.id.miShare).apply {
            isVisible = false
        }
        hideMenuItem = menu.findItem(R.id.miArchive).apply {
            isVisible = false
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.miDelete -> {
                binding.fullProgress.isVisible = true
                if (isPosted) mainViewModel.removePost(run!!.id)
                mainViewModel.deletePositionList(listOf(run!!))
                mainViewModel.deleteRun(listOf(run!!))
                isDeleted = true
                if (!isPosted) {
                    binding.root.createSnackBar(
                        message = "Deleted run successfully",
                        okayAction = true
                    ).show()
                    findNavController().popBackStack()
                }
                true
            }
            R.id.miShare -> {
                run?.let {
                    mainViewModel.insertPost(it, route!!)
                }
                true
            }
            R.id.miArchive -> {
                run?.let {
                    mainViewModel.removePost(it.id)
                }
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