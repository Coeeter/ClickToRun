package com.example.clicktorun.ui.fragments.settings

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.clicktorun.R
import com.example.clicktorun.data.models.User
import com.example.clicktorun.databinding.FragmentSettingsBinding
import com.example.clicktorun.services.RunService
import com.example.clicktorun.ui.activities.AuthActivity
import com.example.clicktorun.ui.viewmodels.AuthViewModel
import com.example.clicktorun.ui.viewmodels.MainViewModel
import com.example.clicktorun.utils.ACTION_NAVIGATE_TO_LOGIN
import com.example.clicktorun.utils.startActivityWithAnimation
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private lateinit var binding: FragmentSettingsBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private var user: User? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)
        setUpObservers()
        setUpListeners()
    }

    private fun setUpObservers() {
        mainViewModel.user.observe(viewLifecycleOwner) {
            binding.imageProgress.visibility = View.VISIBLE
            user = it
            it ?: return@observe
            binding.username.text = user!!.username
            user!!.profileImage
                ?: return@observe binding.profileImage.setImageResource(R.drawable.ic_baseline_person_24)
                    .run { binding.imageProgress.visibility = View.GONE }
            Picasso.with(requireContext())
                .load(user!!.profileImage)
                .into(binding.profileImage, object : Callback {
                    override fun onSuccess() {
                        binding.imageProgress.visibility = View.GONE
                    }

                    override fun onError() {
                        binding.imageProgress.visibility = View.GONE
                        binding.profileImage.setImageResource(R.drawable.ic_baseline_person_24)
                    }
                })
        }
        mainViewModel.getCurrentUser()
    }

    private fun setUpListeners() {
        binding.btnSignOut.setOnClickListener {
            if (RunService.isTracking.value == true) return@setOnClickListener
            authViewModel.signOut()
            Intent(requireContext(), AuthActivity::class.java).run {
                action = ACTION_NAVIGATE_TO_LOGIN
                requireActivity().startActivityWithAnimation(this)
                requireActivity().finish()
            }
        }
        binding.editAccountBtn.setOnClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionMiSettingsToEditAccountFragment()
            )
        }
        binding.deleteAccountBtn.setOnClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionMiSettingsToDeleteAccountFragment()
            )
        }
        binding.details.setOnClickListener {

        }
    }

//    private fun showDeleteAlertDialog(context: Context) {
//        MaterialAlertDialogBuilder(context, R.style.AlertDialogCustom)
//            .setTitle("Delete all Runs?")
//            .setMessage("Are you sure you want to delete all runs and lose its data forever?")
//            .setPositiveButton("Yes") { dialog, _ ->
//                dialog.dismiss()
//                mainViewModel.deleteAllRuns(user!!.email)
//                binding.root.createSnackBar("Successfully deleted all Runs!")
//                    .setAnchorView(binding.anchor)
//                    .show()
//            }
//            .setNegativeButton("No") { dialog, _ ->
//                dialog.dismiss()
//            }
//            .show()
//    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        view?.let { onViewCreated(it, null) }
    }
}