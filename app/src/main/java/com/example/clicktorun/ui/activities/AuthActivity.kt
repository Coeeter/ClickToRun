package com.example.clicktorun.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.clicktorun.AuthNavigationDirections
import com.example.clicktorun.R
import com.example.clicktorun.databinding.ActivityAuthBinding
import com.example.clicktorun.utils.ACTION_NAVIGATE_TO_LOGIN
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (intent.action == ACTION_NAVIGATE_TO_LOGIN)
            findNavController(R.id.authFragmentContainer).navigate(
                AuthNavigationDirections.mainToLogin()
            )
    }
}