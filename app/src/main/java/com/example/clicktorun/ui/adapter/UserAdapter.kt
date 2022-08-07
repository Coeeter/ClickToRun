package com.example.clicktorun.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.clicktorun.R
import com.example.clicktorun.data.models.User
import com.example.clicktorun.databinding.RecyclerviewUserItemBinding
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
        holder.binding.progress.isVisible = false
        holder.binding.profileImage.setImageResource(R.drawable.ic_baseline_person_24)
        if (user.profileImage != null) {
            holder.binding.progress.isVisible = true
            user.profileImage.observe(lifecycleOwner) {
                if (it == null) {
                    holder.binding.profileImage.setImageResource(R.drawable.ic_baseline_person_24)
                    holder.binding.progress.isVisible = false
                    return@observe
                }
                Picasso.with(holder.binding.root.context)
                    .load(it)
                    .into(holder.binding.profileImage, object: Callback {
                        override fun onSuccess() {
                            holder.binding.progress.isVisible = false
                        }

                        override fun onError() {
                            holder.binding.profileImage.setImageResource(R.drawable.ic_baseline_person_24)
                            holder.binding.progress.isVisible = false
                        }
                    })
            }
        }
        holder.binding.username.text = user.username
        holder.binding.root.setOnClickListener {
            navigateToUserDetails(user.email)
        }
    }

    override fun getItemCount() = userList.size

}