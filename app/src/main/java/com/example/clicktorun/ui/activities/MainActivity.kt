package com.example.clicktorun.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.clicktorun.MainNavigationDirections
import com.example.clicktorun.R
import com.example.clicktorun.databinding.ActivityMainBinding
import com.example.clicktorun.services.RunService
import com.example.clicktorun.utils.ACTION_NAVIGATE_TO_TRACKING
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermissions()
        setUpViews()
        checkIfNeedToNavigateToTrackingFragment(intent)
    }

    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) return
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
            103
        )
    }

    private fun setUpViews() {
        navController = findNavController(R.id.fragmentContainerView)
        binding.navBar.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val hideNavigationFragments = listOf(
                R.id.trackingFragment,
                R.id.editAccountFragment,
                R.id.photoBottomSheet,
                R.id.deleteAccountFragment,
                R.id.runDetailsFragment,
            )
            if (destination.id in hideNavigationFragments)
                return@addOnDestinationChangedListener binding.navBar.setVisibility(View.GONE)
            binding.navBar.visibility = View.VISIBLE
        }
    }

    private fun checkIfNeedToNavigateToTrackingFragment(intent: Intent) {
        if (intent.action == ACTION_NAVIGATE_TO_TRACKING ||
            RunService.isTracking.value == true
        ) navController.navigate(MainNavigationDirections.mainToTracking())
    }

}