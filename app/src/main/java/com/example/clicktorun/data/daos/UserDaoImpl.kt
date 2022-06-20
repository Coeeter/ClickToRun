package com.example.clicktorun.data.daos

import android.util.Log
import com.example.clicktorun.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserDaoImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore,
) : UserDao {
    override suspend fun getUser(): User? {
        try {
            val documentSnapshot = firebaseFirestore.collection("users")
                .document(firebaseAuth.currentUser!!.email!!)
                .get()
                .await()
            Log.d("poly", documentSnapshot.toString())
            if (documentSnapshot.exists() &&
                documentSnapshot.contains("username") &&
                documentSnapshot.contains("heightInCentimetres") &&
                documentSnapshot.contains("weightInKilograms")
            ) return User(documentSnapshot)
        } catch (e: Exception) {
            Log.d("poly", e.message.toString())
            e.printStackTrace()
        }
        return null
    }

    override suspend fun insertUser(user: User): Boolean {
        try {
            val map = hashMapOf<String, Any>(
                "username" to user.username,
                "heightInCentimetres" to (user.heightInMetres * 100),
                "weightInKilograms" to (user.weightInKilograms)
            )
            firebaseFirestore.collection("users")
                .document(firebaseAuth.currentUser!!.email!!)
                .set(map)
                .await()
            return true
        } catch (e: Exception) {
            Log.d("poly", e.message.toString())
            e.printStackTrace()
        }
        return false
    }

    override suspend fun updateUser(map: Map<String, Any>): Boolean {
        try {
            firebaseFirestore.collection("users")
                .document(firebaseAuth.currentUser!!.email!!)
                .update(map)
                .await()
            return true
        } catch (e: Exception) {
            Log.d("poly", e.message.toString())
            e.printStackTrace()
        }
        return false
    }

    override suspend fun deleteUser(email: String): Boolean {
        try {
            firebaseFirestore.collection("users")
                .document(firebaseAuth.currentUser!!.email!!)
                .delete()
                .await()
            return true
        } catch (e: Exception) {
            Log.d("poly", e.message.toString())
            e.printStackTrace()
        }
        return false
    }
}