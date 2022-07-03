package com.example.clicktorun.data.daos

import android.net.Uri
import android.util.Log
import com.example.clicktorun.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*

class UserDaoImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage
) : UserDao {
    override suspend fun getUser(): User? {
        try {
            val documentSnapshot = firebaseFirestore.collection("users")
                .document(firebaseAuth.currentUser!!.email!!)
                .get()
                .await()
            if (!documentSnapshot.exists() &&
                !documentSnapshot.contains("username") &&
                !documentSnapshot.contains("heightInCentimetres") &&
                !documentSnapshot.contains("weightInKilograms")
            ) return null
            if (documentSnapshot.contains("profileImage")) {
                val uri = firebaseStorage.reference
                    .child(documentSnapshot.getString("profileImage")!!)
                    .downloadUrl
                    .await()
                return User(documentSnapshot, uri.toString())
            }
            return User(documentSnapshot)
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

    override suspend fun updateUser(map: Map<String, Any>, uri: Uri?): Boolean {
        try {
            uri?.let {
                val path = "images/${UUID.randomUUID()}"
                firebaseStorage.reference
                    .child(path)
                    .putFile(it)
                    .await()
                (map as HashMap).put("profileImage", path)
            }
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