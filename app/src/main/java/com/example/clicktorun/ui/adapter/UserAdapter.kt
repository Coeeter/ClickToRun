package com.example.clicktorun.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.clicktorun.R
import com.example.clicktorun.data.models.User
import com.example.clicktorun.databinding.RecyclerviewUserItemBinding
import com.example.clicktorun.utils.loadImage
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class UserAdapter(
    private val userList: List<User>,
    private val lifecycleOwner: LifecycleOwner,
    private val navigateToUserDetails: (String) -> Unit
): RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(val binding: RecyclerviewUserItemBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerviewUserItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        user.profileImage.loadImage(
            lifecycleOwner,
            holder.binding.profileImage,
            holder.binding.progress
        )
        holder.binding.username.text = user.username
        holder.binding.root.setOnClickListener {
            navigateToUserDetails(user.email)
        }
    }

    override fun getItemCount() = userList.size

}