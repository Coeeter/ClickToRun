package com.example.clicktorun.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clicktorun.data.models.Run
import com.example.clicktorun.databinding.RecyclerviewRunDetailsItemBinding
import com.example.clicktorun.utils.isNightModeEnabled
import com.example.clicktorun.utils.toTimeString
import kotlin.math.round

class RunAdapter(
    private val runList: List<Run>
) : RecyclerView.Adapter<RunAdapter.ViewHolder>() {

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
        }
    }

    override fun getItemCount() = runList.size

    class ViewHolder(val binding: RecyclerviewRunDetailsItemBinding) :
        RecyclerView.ViewHolder(binding.root)

}