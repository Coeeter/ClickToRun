package com.example.clicktorun.ui.fragments.details

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.clicktorun.R
import com.example.clicktorun.data.models.Run
import com.example.clicktorun.databinding.FragmentRunsBinding
import com.example.clicktorun.ui.adapter.RunAdapter
import com.example.clicktorun.ui.viewmodels.MainViewModel
import com.example.clicktorun.utils.createSnackBar
import com.example.clicktorun.utils.isDeviceInLandscape
import com.example.clicktorun.utils.isNightModeEnabled
import com.example.clicktorun.utils.setActionToolbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class YourRunsFragment : Fragment(R.layout.fragment_runs) {
    private lateinit var binding: FragmentRunsBinding
    private val mainViewModel: MainViewModel by activityViewModels()
    private var menuItem: MenuItem? = null
    private var shareMenuItem: MenuItem? = null
    private var hideMenuItem: MenuItem? = null

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
        binding.progress.visibility = View.VISIBLE
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
        mainViewModel.user.observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            mainViewModel.getRunList(user.email).observe(viewLifecycleOwner) {
                if (RunAdapter.selectable) return@observe
                binding.progress.visibility = View.GONE
                binding.noRunsView.visibility = View.GONE
                if (binding.recyclerView.adapter != null) {
                    val adapter = binding.recyclerView.adapter as RunAdapter
                    if (adapter.runList.map { run -> run.id } == it.map { run -> run.id })
                        return@observe
                }
                if (it.isEmpty()) {
                    binding.noRunsView.visibility = View.VISIBLE
                    binding.recyclerView.apply {
                        adapter = null
                        layoutManager = null
                    }
                    return@observe
                }
                binding.recyclerView.adapter = getAdapter(it)
                var layoutManager = LinearLayoutManager(requireContext())
                if (requireContext().isDeviceInLandscape()) {
                    layoutManager = GridLayoutManager(requireContext(), 2)
                }
                binding.recyclerView.layoutManager = layoutManager
            }
        }
        mainViewModel.getUser()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        view?.let { onViewCreated(it, null) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu, menu)
        menuItem = menu.findItem(R.id.miDelete)
        shareMenuItem = menu.findItem(R.id.miShare).apply { isVisible = false }
        hideMenuItem = menu.findItem(R.id.miArchive).apply { isVisible = false }
        if (!RunAdapter.selectable) menuItem?.isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.miDelete) {
            RunAdapter.selectedItems.forEach {
                mainViewModel.removePost(it.id)
            }
            mainViewModel.deletePositionList(RunAdapter.selectedItems)
            mainViewModel.deleteRun(RunAdapter.selectedItems)
            hideActionMenu()
            binding.root.createSnackBar("Run has been deleted successfully").apply {
                anchorView = binding.anchor
                show()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getAdapter(runList: List<Run>) =
        RunAdapter(runList, findNavController(), object : RunAdapter.AdapterListener {
            override fun onItemSizeChanged(size: Int) {
                if (!RunAdapter.selectable) return
                binding.toolbar.title = "$size selected"
            }

            override fun onLongPressed(adapter: RunAdapter) = showActionMenu(adapter)
            override fun navigateToDetailsScreen(run: Run) {
                mainViewModel.setSelectedRun(run)
                findNavController().navigate(
                    YourRunsFragmentDirections.actionMiYourRunsToRunDetailsFragment()
                )
            }
        })

    private fun showActionMenu(adapter: RunAdapter): Boolean {
        menuItem?.isVisible = true
        binding.toolbar.navigationIcon = AppCompatResources.getDrawable(
            requireContext(),
            R.drawable.ic_baseline_close_24
        )
        binding.toolbar.setNavigationOnClickListener {
            hideActionMenu()
            adapter.notifyDataSetChanged()
        }
        return true
    }

    private fun hideActionMenu() {
        RunAdapter.apply {
            selectable = false
            selectedItems.clear()
        }
        menuItem?.isVisible = false
        binding.toolbar.navigationIcon = null
        binding.toolbar.title = "Your Runs"
    }
}