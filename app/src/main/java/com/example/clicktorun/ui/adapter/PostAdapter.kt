package com.example.clicktorun.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.clicktorun.R
import com.example.clicktorun.data.models.Post
import com.example.clicktorun.data.models.User
import com.example.clicktorun.databinding.RecyclerviewPostItemBinding
import com.example.clicktorun.utils.getDate
import com.example.clicktorun.utils.getTime
import com.example.clicktorun.utils.isNightModeEnabled
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class PostAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val listener: Listener
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    var postList = listOf<Post>()
    var followingList = listOf<User>()
    var isProfileDetail = false

    class ViewHolder(
        val binding: RecyclerviewPostItemBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerviewPostItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = postList[position]
        var isFollowingUser = false
        if (followingList.map { it.email }.contains(post.run.email)) {
            isFollowingUser = true
        }

        val callback = object : Callback {
            override fun onSuccess() {
                holder.binding.runImageProgress.isVisible = false
            }

            override fun onError() {
                holder.binding.runImageProgress.isVisible = false
                holder.binding.runImage.setImageResource(R.drawable.ic_baseline_directions_run_24)
            }
        }

        post.lightModeImage?.observe(lifecycleOwner) {
            it ?: return@observe
            if (holder.binding.root.context.isNightModeEnabled()) return@observe
            Picasso.with(holder.binding.root.context).load(it).into(holder.binding.runImage, callback)
        }

        post.darkModeImage?.observe(lifecycleOwner) {
            it ?: return@observe
            if (!holder.binding.root.context.isNightModeEnabled()) return@observe
            Picasso.with(holder.binding.root.context).load(it).into(holder.binding.runImage, callback)
        }

        val timeStarted = post.run.timeEnded - post.run.timeTakenInMilliseconds
        holder.binding.timeStartedTxt.text = "${timeStarted.getTime()} - ${timeStarted.getDate()}"

        holder.binding.root.setOnClickListener {
            listener.setSelectedPost(post)
        }
        if (isProfileDetail) {
            holder.binding.userDetails.isVisible = false
            holder.binding.followBtn.isVisible = false
            holder.binding.hidePostBtn.isVisible = false
            return
        }
        holder.binding.apply {
            userDetails.isVisible = true
            followBtn.isVisible = true
            hidePostBtn.isVisible = false
            post.profileImage?.observe(lifecycleOwner) {
                profileProgress.isVisible = true
                it ?: return@observe run {
                    profileProgress.isVisible = false
                    profileImage.setImageResource(R.drawable.ic_baseline_person_24)
                }
                Picasso.with(root.context).load(it).into(profileImage, object : Callback {
                    override fun onSuccess() {
                        profileProgress.isVisible = false
                    }

                    override fun onError() {
                        profileProgress.isVisible = false
                        profileImage.setImageResource(R.drawable.ic_baseline_person_24)
                    }
                })
            }
            username.text = post.username
            if (post.isCurrentUser) {
                followBtn.isVisible = false
                hidePostBtn.isVisible = true
            }
            followBtn.text = "Follow"
            if (isFollowingUser) {
                followBtn.text = "Unfollow"
            }
            hidePostBtn.setOnClickListener {
                if (!post.isCurrentUser) return@setOnClickListener
                listener.hidePost(post.run.id)
            }
            followBtn.setOnClickListener {
                if (post.isCurrentUser) return@setOnClickListener
                if (isFollowingUser) {
                    listener.unfollowUser(post.run.email!!)
                    return@setOnClickListener
                }
                listener.followUser(post.run.email!!)
            }
            userDetails.setOnClickListener {
                listener.navigateToUserDetails(post.run.email!!)
            }
        }
    }

    override fun getItemCount() = postList.size

    interface Listener {
        fun setSelectedPost(post: Post)
        fun hidePost(runId: String)
        fun followUser(email: String)
        fun unfollowUser(email: String)
        fun navigateToUserDetails(email: String)
    }

}