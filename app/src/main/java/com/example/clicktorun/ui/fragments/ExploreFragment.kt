package com.example.clicktorun.ui.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.clicktorun.R
import com.example.clicktorun.databinding.FragmentExploreBinding

class ExploreFragment : Fragment(R.layout.fragment_explore) {
    private lateinit var binding: FragmentExploreBinding

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
        binding = FragmentExploreBinding.bind(view)
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        view?.let { onViewCreated(it, null) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.explore_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}