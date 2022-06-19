package com.example.clicktorun.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clicktorun.R
import com.example.clicktorun.data.models.Run
import com.example.clicktorun.databinding.RecyclerviewRunDetailsItemBinding
import com.example.clicktorun.repositories.UserRepository
import com.example.clicktorun.utils.formatDistance
import com.example.clicktorun.utils.isNightModeEnabled
import com.example.clicktorun.utils.toTimeString
import javax.inject.Inject

class RunAdapter @Inject constructor(
    private val userRepository: UserRepository
) : RecyclerView.Adapter<RunAdapter.ViewHolder>() {

    private var _runList = listOf<Run>()
    fun setRunList(runList: List<Run>) {
        _runList = runList
        notifyDataSetChanged()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
//        recyclerView.layoutManager = GridLayoutManager(recyclerView.context, 2)
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
        val run = _runList[position]
        holder.binding.apply {
            runImage.setImageBitmap(run.lightModeImage)
            details.background = AppCompatResources.getDrawable(
                root.context,
                R.drawable.custom_light_mode_background
            )
            if (root.context.isNightModeEnabled()) {
                runImage.setImageBitmap(run.darkModeImage)
                details.background = AppCompatResources.getDrawable(
                    root.context,
                    R.drawable.custom_dark_mode_background
                )
            }
            distanceRan.text = run.distanceRanInMetres.formatDistance()
            timeTaken.text = run.timeTakenInMilliseconds.toTimeString()
            averageSpeed.text = "${run.averageSpeedInKilometersPerHour!!.toInt()}km/h"
            caloriesBurnt.text = "${run.caloriesBurnt!!.toInt()}kcal"
        }
    }

    override fun getItemCount() = _runList.size

    class ViewHolder(val binding: RecyclerviewRunDetailsItemBinding) :
        RecyclerView.ViewHolder(binding.root)

}