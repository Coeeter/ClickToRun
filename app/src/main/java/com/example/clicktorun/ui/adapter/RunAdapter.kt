package com.example.clicktorun.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clicktorun.databinding.RecyclerRunItemBinding
import com.example.clicktorun.db.Run

class RunAdapter : RecyclerView.Adapter<RunAdapter.ViewHolder>() {
    private var _runList = listOf<Run>()
    fun setRunList(runList: List<Run>) {
        _runList = runList
        notifyDataSetChanged()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        RecyclerRunItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val run = _runList[position]
        holder.binding.runImage.setImageBitmap(run.bitmap)
    }

    override fun getItemCount() = _runList.size

    class ViewHolder(val binding: RecyclerRunItemBinding) : RecyclerView.ViewHolder(binding.root)

}