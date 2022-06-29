package com.example.clicktorun.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.size
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clicktorun.data.models.Run
import com.example.clicktorun.databinding.RecyclerviewRunDetailsItemBinding
import com.example.clicktorun.utils.isNightModeEnabled
import com.example.clicktorun.utils.toTimeString
import kotlin.math.round

class RunAdapter(
    val runList: List<Run>,
    private val onLongPressed: (RunAdapter) -> Boolean,
    private val onItemSizeChanged: (Int) -> Unit
) : RecyclerView.Adapter<RunAdapter.ViewHolder>() {

    companion object {
        var selectable = false
        val selectedItems = mutableListOf<Run>()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        RecyclerviewRunDetailsItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val run = runList[position]
        holder.binding.apply {
            selected.visibility = View.GONE
            runImage.setImageBitmap(run.lightModeImage)
            if (holder.binding.root.context.isNightModeEnabled())
                runImage.setImageBitmap(run.darkModeImage)
            distanceLabel.text = "Km"
            distanceRan.text = (run.distanceRanInMetres / 1000.0).toString()
            if (run.distanceRanInMetres < 1000) {
                distanceLabel.text = "m"
                distanceRan.text = run.distanceRanInMetres.toString()
            }
            timeTaken.text = run.timeTakenInMilliseconds.toTimeString()
            averageSpeed.text = "${round(run.averageSpeedInKilometersPerHour!! * 100) / 100.0}"
            caloriesBurnt.text = "${run.caloriesBurnt!!.toInt()}"
            root.setOnLongClickListener {
                selected.visibility = View.VISIBLE
                selectable = true
                selectedItems.add(run)
                onItemSizeChanged(selectedItems.size)
                onLongPressed(this@RunAdapter)
            }
            root.setOnClickListener {
                if (!selectable) return@setOnClickListener
                if (run in selectedItems) {
                    selectedItems.remove(run)
                    selected.visibility = View.GONE
                    onItemSizeChanged(selectedItems.size)
                    return@setOnClickListener
                }
                selectedItems.add(run)
                onItemSizeChanged(selectedItems.size)
                selected.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount() = runList.size

    class ViewHolder(val binding: RecyclerviewRunDetailsItemBinding) :
        RecyclerView.ViewHolder(binding.root)

}