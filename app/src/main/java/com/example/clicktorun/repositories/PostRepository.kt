package com.example.clicktorun.repositories

import com.example.clicktorun.data.daos.PostDao
import com.example.clicktorun.data.models.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val postDao: PostDao,
) {
    fun getAllPosts() = postDao.getAllPosts()

    fun getPostOfUser(email: String) = postDao.getPostOfUser(email)

    suspend fun insertPost(post: Post) = withContext(Dispatchers.IO) {
        postDao.insertPost(post)
    }

    suspend fun insertMultiplePosts(posts: List<Post>) = withContext(Dispatchers.IO) {
        postDao.insertMultiplePosts(posts)
    }

    suspend fun deletePost(runId: String) = withContext(Dispatchers.IO) {
        postDao.deletePost(runId)
    }

    suspend fun deleteAllPosts() = withContext(Dispatchers.IO) {
        postDao.deleteAllPosts()
    }
}