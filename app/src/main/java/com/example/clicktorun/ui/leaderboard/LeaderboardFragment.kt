package com.example.clicktorun.ui.leaderboard

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.clicktorun.R
import com.example.clicktorun.databinding.FragmentLeaderboardBinding

class LeaderboardFragment : Fragment(R.layout.fragment_leaderboard) {
    private lateinit var binding: FragmentLeaderboardBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLeaderboardBinding.bind(view)
    }
}