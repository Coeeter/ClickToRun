package com.example.clicktorun.repositories

import android.net.Uri
import com.example.clicktorun.data.daos.UserDao
import com.example.clicktorun.data.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    suspend fun getUser(email: String? = null) = withContext(Dispatchers.IO) {
        userDao.getUser(email)
    }

    suspend fun insertUser(user: User) = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(map: Map<String, Any>, uri: Uri? = null) = withContext(Dispatchers.IO) {
        userDao.updateUser(map, uri)
    }

    suspend fun deleteUser(email: String) = withContext(Dispatchers.IO) {
        userDao.deleteUser(email)
    }

    suspend fun deleteImage(email: String) = withContext(Dispatchers.IO) {
        userDao.deleteProfileImage(email)
    }
}