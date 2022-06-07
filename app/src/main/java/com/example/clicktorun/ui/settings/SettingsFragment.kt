package com.example.clicktorun.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.clicktorun.R
import com.example.clicktorun.databinding.FragmentSettingsBinding
import com.example.clicktorun.ui.auth.LoginActivity
import com.example.clicktorun.utils.startActivityWithAnimation
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private lateinit var binding: FragmentSettingsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)
        binding.btnSignOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Intent(requireContext(), LoginActivity::class.java).let {
                requireActivity().apply {
                    startActivityWithAnimation(it)
                    finish()
                }
            }
        }
    }
}