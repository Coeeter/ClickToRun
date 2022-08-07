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
        holder.binding.apply {
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
            val callback = object : Callback {
                override fun onSuccess() {
                    runImageProgress.isVisible = false
                }

                override fun onError() {
                    runImageProgress.isVisible = false
                    runImage.setImageResource(R.drawable.ic_baseline_directions_run_24)
                }
            }
            post.lightModeImage?.observe(lifecycleOwner) {
                it ?: return@observe
                if (root.context.isNightModeEnabled()) return@observe
                Picasso.with(root.context).load(it).into(runImage, callback)
            }
            post.darkModeImage?.observe(lifecycleOwner) {
                it ?: return@observe
                if (!root.context.isNightModeEnabled()) return@observe
                Picasso.with(root.context).load(it).into(runImage, callback)
            }
            username.text = post.username
            val timeStarted = post.run.timeEnded - post.run.timeTakenInMilliseconds
            timeStartedTxt.text = "${timeStarted.getTime()} - ${timeStarted.getDate()}"
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
                    listener.unFollowUser(post.run.email!!)
                    return@setOnClickListener
                }
                listener.followUser(post.run.email!!)
            }
            root.setOnClickListener {
                listener.setSelectedPost(post)
            }
        }
    }

    override fun getItemCount() = postList.size

    interface Listener {
        fun setSelectedPost(post: Post)
        fun hidePost(runId: String)
        fun followUser(email: String)
        fun unFollowUser(email: String)
    }

}