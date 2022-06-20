package com.example.clicktorun.repositories

import com.example.clicktorun.data.daos.UserDao
import com.example.clicktorun.data.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    suspend fun getCurrentUser() = withContext(Dispatchers.IO) {
        userDao.getUser()
    }

    suspend fun insertUser(user: User) = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(map: Map<String, Any>) = withContext(Dispatchers.IO) {
        userDao.updateUser(map)
    }

    suspend fun deleteUser(email: String) = withContext(Dispatchers.IO) {
        userDao.deleteUser(email)
    }
}