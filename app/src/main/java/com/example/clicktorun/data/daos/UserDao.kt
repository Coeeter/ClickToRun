package com.example.clicktorun.data.daos

import com.example.clicktorun.data.models.User

interface UserDao {
    suspend fun getUser(): User?
    suspend fun insertUser(user: User): Boolean
    suspend fun updateUser(map: Map<String, Any>): Boolean
    suspend fun deleteUser(email: String): Boolean
}