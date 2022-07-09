package com.example.clicktorun.data.daos

import android.net.Uri
import com.example.clicktorun.data.models.User

interface UserDao {
    suspend fun getUser(): User?
    suspend fun insertUser(user: User): Boolean
    suspend fun updateUser(map: Map<String, Any>, uri: Uri? = null): Boolean
    suspend fun deleteUser(email: String): Boolean
    suspend fun deleteProfileImage(email: String): Boolean
}