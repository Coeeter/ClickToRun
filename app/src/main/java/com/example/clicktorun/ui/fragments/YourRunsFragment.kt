package com.example.clicktorun.ui.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.clicktorun.R
import com.example.clicktorun.data.models.Run
import com.example.clicktorun.databinding.FragmentRunsBinding
import com.example.clicktorun.ui.adapter.RunAdapter
import com.example.clicktorun.ui.viewmodels.TrackingViewModel
import com.example.clicktorun.utils.isNightModeEnabled
import com.example.clicktorun.utils.setActionToolbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class YourRunsFragment : Fragment(R.layout.fragment_runs) {
    private lateinit var binding: FragmentRunsBinding
    private val trackingViewModel: TrackingViewModel by viewModels()
    private val runList = mutableListOf<Run>()
    private lateinit var menuItem: MenuItem

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
        binding = FragmentRunsBinding.bind(view)
        requireActivity().setActionToolbar(binding.toolbar)
        binding.btnAddRun.background = AppCompatResources.getDrawable(
            requireContext(),
            R.drawable.custom_fab_light_mode_background
        )
        if (requireContext().isNightModeEnabled()) {
            binding.btnAddRun.background = AppCompatResources.getDrawable(
                requireContext(),
                R.drawable.custom_fab_dark_mode_background
            )
        }
        binding.btnAddRun.setOnClickListener {
            findNavController().navigate(YourRunsFragmentDirections.runsToTracking())
        }
        trackingViewModel.user.observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            trackingViewModel.getRunList(user.email).observe(viewLifecycleOwner) {
                menuItem.isVisible = false
                runList.clear()
                runList.addAll(it)
                binding.noRunsView.visibility = View.GONE
                binding.recyclerView.adapter = RunAdapter(runList, { adapter ->
                    menuItem.isVisible = true
                    binding.toolbar.navigationIcon = AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.ic_baseline_close_24,
                    )
                    binding.toolbar.setNavigationOnClickListener {
                        binding.toolbar.navigationIcon = null
                        menuItem.isVisible = false
                        binding.toolbar.title = "Your Runs"
                        RunAdapter.selectable = false
                        RunAdapter.selectedItems.clear()
                        adapter.notifyDataSetChanged()
                    }
                    true
                }, { size ->
                    if (!RunAdapter.selectable) return@RunAdapter
                    binding.toolbar.title = "$size selected"
                })
                if (it.isEmpty()) binding.noRunsView.visibility = View.VISIBLE
            }
        }
        trackingViewModel.getCurrentUser()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        view?.let { onViewCreated(it, null) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu, menu)
        menuItem = menu.findItem(R.id.miDelete)
        menuItem.isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.miDelete) {
            trackingViewModel.deleteRun(RunAdapter.selectedItems)
            RunAdapter.selectedItems.clear()
            RunAdapter.selectable = false
            binding.toolbar.title = "Your Runs"
            binding.toolbar.navigationIcon = null
            menuItem.isVisible = false
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}