package com.example.clicktorun.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.clicktorun.R
import com.example.clicktorun.data.models.User
import com.example.clicktorun.databinding.FragmentSettingsBinding
import com.example.clicktorun.services.RunService
import com.example.clicktorun.ui.activities.AuthActivity
import com.example.clicktorun.ui.viewmodels.AuthViewModel
import com.example.clicktorun.ui.viewmodels.MainViewModel
import com.example.clicktorun.utils.ACTION_NAVIGATE_TO_LOGIN
import com.example.clicktorun.utils.REQUEST_CODE_TO_PICK_IMAGE
import com.example.clicktorun.utils.createSnackBar
import com.example.clicktorun.utils.startActivityWithAnimation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        mainViewModel.user.observe(viewLifecycleOwner) {
            user = it
            it ?: return@observe
            binding.usernameInput.editText?.setText(user!!.username)
            binding.heightInput.editText?.setText((user!!.heightInMetres * 100).toString())
            binding.weightInput.editText?.setText(user!!.weightInKilograms.toString())
            user!!.profileImage?.let { profileImage ->
                Picasso.with(requireContext()).load(profileImage).into(binding.profileImage)
            }
        }
        authViewModel.authState.observe(viewLifecycleOwner) {
            when (it) {
                is AuthViewModel.AuthState.Success -> {
                    binding.root.createSnackBar("Updated Account Successfully").show()
                }
                is AuthViewModel.AuthState.FireBaseFailure -> {
                    binding.root.createSnackBar(it.message ?: "Unknown error has occurred").show()
                }
                is AuthViewModel.AuthState.InvalidUsername -> {
                    binding.usernameInput.isErrorEnabled = true
                    binding.usernameInput.error = it.message
                }
                is AuthViewModel.AuthState.InvalidHeight -> {
                    binding.heightInput.isErrorEnabled = true
                    binding.heightInput.error = it.message
                }
                is AuthViewModel.AuthState.InvalidWeight -> {
                    binding.weightInput.isErrorEnabled = true
                    binding.weightInput.error = it.message
                }
                else -> {
                }
            }
        }
        mainViewModel.getCurrentUser()
        setUpListeners()
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
        binding.btnDeleteAllRuns.setOnClickListener {
            showDeleteAlertDialog(requireContext())
        }
        binding.usernameInput.editText?.addTextChangedListener {
            authViewModel.username = it.toString()
            binding.usernameInput.isErrorEnabled = false
        }
        binding.heightInput.editText?.addTextChangedListener {
            authViewModel.height = it.toString()
            binding.heightInput.isErrorEnabled = false
        }
        binding.weightInput.editText?.addTextChangedListener {
            authViewModel.weight = it.toString()
            binding.weightInput.isErrorEnabled = false
        }
        binding.btnSubmit.setOnClickListener { authViewModel.updateUser() }
        binding.profileImage.setOnClickListener {
            startActivityForResult(
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                REQUEST_CODE_TO_PICK_IMAGE
            )
        }
    }

    private fun showDeleteAlertDialog(context: Context) {
        MaterialAlertDialogBuilder(context, R.style.AlertDialogCustom)
            .setTitle("Delete all Runs?")
            .setMessage("Are you sure you want to delete all runs and lose its data forever?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                mainViewModel.deleteAllRuns(user!!.email)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_TO_PICK_IMAGE &&
            resultCode == Activity.RESULT_OK &&
            data != null &&
            data.data != null
        ) {
            binding.profileImage.setImageURI(data.data)
            authViewModel.uri = data.data
        }
    }
}