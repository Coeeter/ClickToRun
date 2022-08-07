package com.example.clicktorun.data.daos

import androidx.lifecycle.LiveData
import com.example.clicktorun.data.models.FollowLink
import com.example.clicktorun.data.models.User

interface FollowDao {
    fun getAllFollowers(email: String): LiveData<List<User>>
    fun getAllUserIsFollowing(email: String): LiveData<List<User>>
    suspend fun insertFollowLink(followLink: FollowLink): Boolean
    suspend fun removeFollowLink(followLink: FollowLink): Boolean
    suspend fun removeAllFollowLinkRelatedToUser(email: String): Boolean
}