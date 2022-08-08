package com.example.clicktorun.ui.fragments.settings

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.clicktorun.R
import com.example.clicktorun.databinding.FragmentEditAccountBinding
import com.example.clicktorun.ui.viewmodels.AuthViewModel
import com.example.clicktorun.ui.viewmodels.MainViewModel
import com.example.clicktorun.utils.createSnackBar
import com.example.clicktorun.utils.loadImage
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditAccountFragment : Fragment(R.layout.fragment_edit_account) {
    private lateinit var binding: FragmentEditAccountBinding
    private val authViewModel: AuthViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private var snackbar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditAccountBinding.bind(view)
        setUpObservers()
        setUpUiListeners()
    }

    private fun setUpObservers() {
        binding.imageProgress.visibility = View.VISIBLE
        binding.btnSubmit.isEnabled = false
        mainViewModel.user.observe(viewLifecycleOwner) {
            it ?: return@observe
            binding.apply {
                usernameInput.editText?.setText(it.username)
                heightInput.editText?.setText("${it.heightInMetres * 100}")
                weightInput.editText?.setText("${it.weightInKilograms}")
                btnSubmit.isEnabled = true
            }
            it.profileImage.loadImage(
                viewLifecycleOwner,
                binding.profileImage,
                binding.imageProgress
            )
        }
        mainViewModel.getUser()
        authViewModel.authState.observe(viewLifecycleOwner) {
            when (it) {
                is AuthViewModel.AuthState.Loading -> {
                    binding.mainProgress.visibility = View.VISIBLE
                    authViewModel.uri.let { uri ->
                        if (uri == null) return@let binding.profileImage.setImageResource(R.drawable.ic_baseline_person_24)
                        binding.profileImage.setImageURI(uri)
                    }
                }
                is AuthViewModel.AuthState.Success -> {
                    if (!binding.mainProgress.isVisible) return@observe
                    binding.mainProgress.visibility = View.GONE
                    createSnackbar("Account Updated Successfully", true)
                    binding.btnSubmit.isEnabled = false
                }
                is AuthViewModel.AuthState.FireBaseFailure -> {
                    if (!binding.mainProgress.isVisible) return@observe
                    binding.mainProgress.visibility = View.GONE
                    createSnackbar(it.message ?: "Unknown error has occurred", false)
                }
                is AuthViewModel.AuthState.InvalidUsername -> showError(
                    binding.usernameInput,
                    it.message
                )
                is AuthViewModel.AuthState.InvalidWeight -> showError(
                    binding.weightInput,
                    it.message
                )
                is AuthViewModel.AuthState.InvalidHeight -> showError(
                    binding.heightInput,
                    it.message
                )
                else -> {
                    binding.mainProgress.visibility = View.GONE
                }
            }
        }
    }

    private fun setUpUiListeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (authViewModel.authState.value is AuthViewModel.AuthState.Loading)
                return@setNavigationOnClickListener
            snackbar ?: return@setNavigationOnClickListener run {
                findNavController().popBackStack()
            }
            snackbar!!.dismiss()
            if (authViewModel.authState.value is AuthViewModel.AuthState.FireBaseFailure)
                findNavController().popBackStack()
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
            if (binding.imageProgress.isVisible) return@setOnClickListener
            findNavController().navigate(
                EditAccountFragmentDirections.actionEditAccountFragmentToPhotoBottomSheet()
            )
        }
    }

    private fun createSnackbar(message: String, callback: Boolean) {
        snackbar = binding.root.createSnackBar(message = message, okayAction = true).apply {
            addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar?>() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    if (!callback) return run {
                        snackbar = null
                    }
                    if (findNavController().currentDestination?.id == R.id.editAccountFragment)
                        findNavController().popBackStack()
                }

                override fun onShown(transientBottomBar: Snackbar?) {
                    super.onShown(transientBottomBar)
                    if (!callback) return
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(3000L)
                        if (transientBottomBar?.isShown == true)
                            transientBottomBar.dismiss()
                    }
                }
            })
        }
        snackbar?.show()
    }

    private fun showError(input: TextInputLayout, message: String) {
        binding.mainProgress.visibility = View.GONE
        input.isErrorEnabled = true
        input.error = message
    }
}