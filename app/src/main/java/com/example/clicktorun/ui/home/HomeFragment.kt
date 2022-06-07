package com.example.clicktorun.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.clicktorun.R
import com.example.clicktorun.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
    }
}