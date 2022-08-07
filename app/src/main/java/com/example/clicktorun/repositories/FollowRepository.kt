package com.example.clicktorun.repositories

import com.example.clicktorun.data.daos.FollowDao
import com.example.clicktorun.data.models.FollowLink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FollowRepository @Inject constructor(
    private val followDao: FollowDao,
) {
    fun getAllFollowers(email: String) = followDao.getAllFollowers(email)
    fun getAllFollowing(email: String) = followDao.getAllUserIsFollowing(email)

    suspend fun insertFollow(
        userBeingFollowedEmail: String,
        userFollowingEmail: String
    ) = withContext(Dispatchers.IO) {
        followDao.insertFollowLink(
            FollowLink(
                userFollowingEmail,
                userBeingFollowedEmail
            )
        )
    }

    suspend fun deleteFollow(
        userBeingFollowedEmail: String,
        userFollowingEmail: String
    ) = withContext(Dispatchers.IO) {
        followDao.removeFollowLink(
            FollowLink(
                userFollowingEmail,
                userBeingFollowedEmail
            )
        )
    }

    suspend fun deleteAllFollowLinksRelatedToUser(
        email: String,
    ) = withContext(Dispatchers.IO) {
        followDao.removeAllFollowLinkRelatedToUser(email)
    }
}