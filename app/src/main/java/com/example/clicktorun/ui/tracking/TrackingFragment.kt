package com.example.clicktorun.ui.tracking

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.clicktorun.R
import com.example.clicktorun.data.models.Run
import com.example.clicktorun.databinding.FragmentTrackingBinding
import com.example.clicktorun.services.Line
import com.example.clicktorun.services.RunService
import com.example.clicktorun.ui.auth.UserDetailsActivity
import com.example.clicktorun.utils.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {
    private lateinit var binding: FragmentTrackingBinding
    private var googleMap: GoogleMap? = null
    private val trackingViewModel: TrackingViewModel by viewModels()
    private var runPath: MutableList<Line> = mutableListOf()
    private var distanceInMetres = 0
    private var timeTakenInMilliseconds = 0L
    private var isDarkModeEnabled = false
    private var weight = 60.0
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
        (requireActivity() as AppCompatActivity).setSupportActionBar(
            binding.toolbar.apply { title = "" }
        )
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
        trackingViewModel.user.observe(viewLifecycleOwner) {
            it ?: return@observe run {
                Intent(requireContext(), UserDetailsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    requireActivity().startActivityWithAnimation(this)
                }
            }
            weight = it.weightInKilograms
            email = it.email
        }
        trackingViewModel.getCurrentUser()
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
            runPath = it
            if (RunService.isTracking.value == false) return@observe
            if (it.size == 0 || it.last().size == 0) return@observe
            googleMap?.setOnMapLoadedCallback {
                for (i in it.indices) {
                    if (RunService.isTracking.value == false) return@setOnMapLoadedCallback
                    googleMap?.addPolyline(PolylineOptions().apply {
                        color(requireContext().getColor(R.color.primary))
                        width(8f)
                        addAll(it[i])
                    })
                }
                googleMap?.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.fromLatLngZoom(it.last().last(), 18f)
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
        Handler(requireActivity().mainLooper).postDelayed({
            val latLngBoundsBuilder = LatLngBounds.builder()
            runPath.forEach { line ->
                if (line.isEmpty()) return@forEach
                line.forEach { latLng ->
                    latLngBoundsBuilder.include(latLng)
                }
            }
            googleMap?.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                    latLngBoundsBuilder.build(),
                    binding.map.width,
                    binding.map.height,
                    (binding.map.width * 0.05f).toInt()
                )
            )
            googleMap?.setOnMapLoadedCallback {
                val averageSpeed =
                    round(distanceInMetres / (timeTakenInMilliseconds / 1000.0) * 360) / 100
                val run = Run(
                    email,
                    distanceInMetres,
                    System.currentTimeMillis(),
                    timeTakenInMilliseconds,
                    averageSpeed,
                    getCaloriesBurnt()
                )
                googleMap?.snapshot { firstImage ->
                    configureMapType(googleMap!!, !isDarkModeEnabled)
                    googleMap?.setOnMapLoadedCallback {
                        googleMap?.snapshot { secondImage ->
                            run.apply {
                                lightModeImage = firstImage
                                darkModeImage = secondImage
                            }
                            if (isDarkModeEnabled)
                                run.apply {
                                    lightModeImage = secondImage
                                    darkModeImage = firstImage
                                }
                            trackingViewModel.saveRun(run)
                            cancelRun()
                        }
                    }
                }
            }
        }, 2000)
        return true
    }

    private fun changeMapLayoutParams() {
        val layoutParams = binding.map.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
        layoutParams.dimensionRatio = "h,1:1"
        binding.map.layoutParams = layoutParams
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
        AlertDialog.Builder(requireContext())
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