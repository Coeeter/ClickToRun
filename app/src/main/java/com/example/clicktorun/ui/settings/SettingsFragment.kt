package com.example.clicktorun.ui.settings

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.clicktorun.R
import com.example.clicktorun.data.models.User
import com.example.clicktorun.databinding.FragmentSettingsBinding
import com.example.clicktorun.services.RunService
import com.example.clicktorun.ui.auth.AuthViewModel
import com.example.clicktorun.ui.auth.LoginActivity
import com.example.clicktorun.ui.tracking.TrackingViewModel
import com.example.clicktorun.utils.createSnackBar
import com.example.clicktorun.utils.startActivityWithAnimation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private lateinit var binding: FragmentSettingsBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val trackingViewModel: TrackingViewModel by viewModels()
    private var user: User? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)
        trackingViewModel.user.observe(viewLifecycleOwner) { user = it }
        trackingViewModel.getCurrentUser()
        setUpListeners()
    }

    private fun setUpListeners() {
        binding.btnSignOut.setOnClickListener {
            if (RunService.isTracking.value == true) return@setOnClickListener
            authViewModel.signOut()
            Intent(requireContext(), LoginActivity::class.java).let {
                requireActivity().apply {
                    startActivityWithAnimation(it)
                    finish()
                }
            }
        }
        binding.btnDeleteAllRuns.setOnClickListener {
            showDeleteAlertDialog(requireContext())
        }

    }

    private fun showDeleteAlertDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Delete all Runs?")
            .setMessage("Are you sure you want to delete all runs and lose its data forever?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                trackingViewModel.deleteAllRuns(user!!.email)
                binding.root.createSnackBar("Successfully deleted all Runs!")
                    .setAnchorView(binding.anchor)
                    .show()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        view?.let { onViewCreated(it, null) }
    }
}