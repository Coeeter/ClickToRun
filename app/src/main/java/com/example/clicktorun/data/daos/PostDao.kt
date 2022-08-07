package com.example.clicktorun.data.daos

import androidx.lifecycle.LiveData
import com.example.clicktorun.data.models.Post

interface PostDao {
    fun getAllPosts(): LiveData<List<Post>>
    fun getPostOfUser(email: String): LiveData<List<Post>>
    suspend fun insertPost(post: Post): Boolean
    suspend fun insertMultiplePosts(posts: List<Post>): Boolean
    suspend fun deletePost(runId: String): Boolean
    suspend fun deleteAllPosts(): Boolean
}