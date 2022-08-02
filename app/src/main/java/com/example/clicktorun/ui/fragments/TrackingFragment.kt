package com.example.clicktorun.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.clicktorun.R
import com.example.clicktorun.data.models.Position
import com.example.clicktorun.data.models.Run
import com.example.clicktorun.databinding.FragmentTrackingBinding
import com.example.clicktorun.services.RunService
import com.example.clicktorun.ui.viewmodels.MainViewModel
import com.example.clicktorun.utils.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {
    private lateinit var binding: FragmentTrackingBinding
    private var googleMap: GoogleMap? = null
    private val mainViewModel: MainViewModel by viewModels()
    private var runPath: MutableList<MutableList<Position>> = mutableListOf()
    private var distanceInMetres = 0
    private var timeTakenInMilliseconds = 0L
    private var isDarkModeEnabled = false
    private var weight = 0.0
    private var email = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpVariables(view)
        setUpMap(savedInstanceState)
        setUpServiceListeners()
    }

    private fun setUpVariables(view: View) {
        binding = FragmentTrackingBinding.bind(view)
        requireActivity().setActionToolbar(binding.toolbar.apply { title = "" })
        binding.bottomActionBar.background = AppCompatResources.getDrawable(
            requireContext(),
            R.drawable.custom_light_mode_background
        )
        binding.btnAddRun.setOnClickListener {
            sendCommandToService(
                if (RunService.isTracking.value == true) ACTION_PAUSE_RUN_SERVICE
                else ACTION_START_RUN_SERVICE
            )
        }
        if (requireContext().isNightModeEnabled()) {
            isDarkModeEnabled = true
            binding.bottomActionBar.background = AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.custom_dark_mode_background
            )
        }
        mainViewModel.user.observe(viewLifecycleOwner) {
            it ?: return@observe
            weight = it.weightInKilograms
            email = it.email
        }
        mainViewModel.getCurrentUser()
    }

    private fun setUpMap(savedInstanceState: Bundle?) {
        binding.map.onCreate(savedInstanceState)
        binding.map.getMapAsync {
            googleMap = it.apply {
                mapType = GoogleMap.MAP_TYPE_NORMAL
                configureMapType(this, isDarkModeEnabled)
                val singaporeLocation = LatLng(1.3521, 103.8198)
                moveCamera(CameraUpdateFactory.newLatLngZoom(singaporeLocation, 10f))
            }
        }
    }

    private fun setUpServiceListeners() {
        RunService.runPath.observe(viewLifecycleOwner) {
            runPath = it.map { list ->
                list.mapIndexed { i, pos ->
                    val distanceRan = listOf(list.subList(0, i + 1).map { p ->
                        p.getLatLng()
                    }).getDistance()
                    pos.apply {
                        caloriesBurnt = round(((distanceRan / 1000.0) * weight) * 100) / 100
                    }
                }.toMutableList()
            }.toMutableList()
            if (RunService.isTracking.value == false) return@observe
            if (it.size == 0 || it.last().size == 0) return@observe
            googleMap?.setOnMapLoadedCallback {
                for (i in it.indices) {
                    if (RunService.isTracking.value == false) return@setOnMapLoadedCallback
                    googleMap?.addPolyline(PolylineOptions().apply {
                        color(requireContext().getColor(R.color.primary))
                        width(8f)
                        addAll(it[i].map { pos -> pos.getLatLng() })
                    })
                }
                googleMap?.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.fromLatLngZoom(it.last().last().getLatLng(), 18f)
                    )
                )
            }
        }
        RunService.distanceRanInMetres.observe(viewLifecycleOwner) {
            distanceInMetres = it
            binding.distanceRan.text = distanceInMetres.formatDistance()
        }
        RunService.isTracking.observe(viewLifecycleOwner) {
            binding.btnAddRun.apply {
                setIconResource(if (it == true) R.drawable.ic_pause else R.drawable.ic_play)
                text = if (it == true) "Pause" else "Play"
            }
        }
        RunService.timeTaken.observe(viewLifecycleOwner) {
            it ?: return@observe
            timeTakenInMilliseconds = it
            binding.timeTaken.text = timeTakenInMilliseconds.toTimeString()
        }
    }

    private fun saveRun(): Boolean {
        binding.loading.visibility = View.VISIBLE
        changeMapLayoutParams()
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000L)
            googleMap?.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                    getLatLngBounds(
                        runPath.map { list ->
                            list.map { it.getLatLng() }.toMutableList()
                        }.toMutableList(),
                    ).build(),
                    binding.map.width,
                    binding.map.height,
                    (binding.map.width * 0.05f).toInt()
                )
            )
            googleMap?.setOnMapLoadedCallback {
                val averageSpeed =
                    round(distanceInMetres / (timeTakenInMilliseconds / 1000.0) * 360) / 100
                val run = Run(
                    id = "",
                    email = email,
                    distanceRanInMetres = distanceInMetres,
                    timeEnded = System.currentTimeMillis(),
                    timeTakenInMilliseconds = timeTakenInMilliseconds,
                    averageSpeedInKilometersPerHour = averageSpeed,
                    caloriesBurnt = getCaloriesBurnt(),
                )
                googleMap?.snapshot { firstImage ->
                    configureMapType(googleMap!!, !isDarkModeEnabled)
                    googleMap?.setOnMapLoadedCallback {
                        googleMap?.snapshot { secondImage ->
                            run.apply {
                                lightModeImage = if (!isDarkModeEnabled) firstImage else secondImage
                                darkModeImage = if (!isDarkModeEnabled) secondImage else firstImage
                            }
                            val runId = mainViewModel.saveRun(run)
                            mainViewModel.insertPositionList(
                                runPath.map { list ->
                                    list.map { position ->
                                        position.apply { this.runId = runId }
                                    }
                                }
                            )
                            cancelRun()
                        }
                    }
                }
            }
        }
        return true
    }

    private fun changeMapLayoutParams() {
        val layoutParams = binding.map.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
        layoutParams.dimensionRatio = "h,2:1"
        binding.map.layoutParams = layoutParams
    }

    private fun getLatLngBounds(list: MutableList<MutableList<LatLng>>) =
        LatLngBounds.Builder().apply {
            list.forEach { line ->
                if (line.isEmpty()) return@forEach
                line.forEach { include(it) }
            }
        }

    private fun getCaloriesBurnt() = round(((distanceInMetres / 1000.0) * weight) * 100) / 100

    private fun sendCommandToService(command: String) {
        Intent(requireContext(), RunService::class.java).run {
            action = command
            requireActivity().startService(this)
        }
    }

    private fun configureMapType(map: GoogleMap, isDarkModeEnabled: Boolean) {
        if (isDarkModeEnabled)
            return run {
                map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.map_night_style
                    )
                )
            }
        map.setMapStyle(MapStyleOptions("[]"))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.running_mode_menu, menu)
        val menuItem = menu.findItem(R.id.miSave)
        menuItem.isVisible = false
        RunService.isTracking.observe(viewLifecycleOwner) {
            if (!checkRunPath() || timeTakenInMilliseconds == 0L) return@observe
            if (it == null || it) {
                menuItem.isVisible = false
                return@observe
            }
            menuItem.isVisible = true
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun checkRunPath(): Boolean {
        for (list in runPath)
            if (list.isNotEmpty())
                return true
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.miCancel -> showDialog()
            R.id.miSave -> saveRun()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDialog(): Boolean {
        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogCustom)
            .setTitle("Cancel the run?")
            .setMessage("Are you sure you want to delete the current run and lose all its data forever?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog?.dismiss()
                cancelRun()
            }.setNegativeButton("No") { dialog, _ ->
                dialog?.dismiss()
            }.show()
        return true
    }

    private fun cancelRun() {
        sendCommandToService(ACTION_CANCEL_RUN_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_miHome)
    }

    override fun onStart() {
        super.onStart()
        binding.map.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.map.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.map.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        binding.map.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.map.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.map.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.map.onSaveInstanceState(outState)
    }

}