package com.example.clicktorun.data.impls

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.clicktorun.data.daos.FollowDao
import com.example.clicktorun.data.models.FollowLink
import com.example.clicktorun.data.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FollowDaoImpl(
    private val fireStore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
) : FollowDao {
    private suspend fun getUser(email: String): User {
        val userDoc = fireStore.collection("users")
            .document(email)
            .get()
            .await()
        if (userDoc.contains("profileImage")) {
            val imageLiveData = MutableLiveData<String?>()
            firebaseStorage.reference
                .child(userDoc.getString("profileImage")!!)
                .downloadUrl
                .addOnCompleteListener {
                    if (!it.isSuccessful || it.exception != null) {
                        Log.d("poly", it.exception!!.message.toString())
                        it.exception!!.printStackTrace()
                        if (firebaseAuth.currentUser == null) return@addOnCompleteListener
                        imageLiveData.value = null
                    }
                    if (firebaseAuth.currentUser == null) return@addOnCompleteListener
                    imageLiveData.value = it.result.toString()
                }
            return User(userDoc, imageLiveData)
        }
        return User(userDoc)
    }

    override fun getAllFollowers(email: String): LiveData<List<User>> {
        val followersLiveData = MutableLiveData<List<User>>()
        fireStore.collection("followLinks")
            .whereEqualTo("userBeingFollowed", email)
            .addSnapshotListener { querySnapshot, e ->
                CoroutineScope(Dispatchers.Main).launch {
                    if (e != null) {
                        Log.d("poly", e.message.toString())
                        e.printStackTrace()
                        return@launch
                    }
                    val userList = mutableListOf<User>()
                    for (document in querySnapshot!!.documents) {
                        val followLink = FollowLink(document)
                        userList.add(getUser(followLink.userFollowingEmail))
                    }
                    followersLiveData.value = userList
                }
            }
        return followersLiveData
    }

    override fun getAllUserIsFollowing(email: String): LiveData<List<User>> {
        val followingLiveData = MutableLiveData<List<User>>()
        fireStore.collection("followLinks")
            .whereEqualTo("userFollowing", email)
            .addSnapshotListener { querySnapshot, e ->
                CoroutineScope(Dispatchers.Main).launch {
                    if (e != null) {
                        Log.d("poly", e.message.toString())
                        e.printStackTrace()
                        return@launch
                    }
                    val userList = mutableListOf<User>()
                    for (document in querySnapshot!!.documents) {
                        val followLink = FollowLink(document)
                        userList.add(getUser(followLink.userBeingFollowedEmail))
                    }
                    followingLiveData.value = userList
                }
            }
        return followingLiveData
    }

    override suspend fun insertFollowLink(followLink: FollowLink): Boolean {
        try {
            fireStore.collection("followLinks")
                .document()
                .set(followLink.toMap())
                .await()
            return true
        } catch(e: Exception) {
            Log.d("poly", e.message.toString())
            e.printStackTrace()
        }
        return false
    }

    override suspend fun removeFollowLink(followLink: FollowLink): Boolean {
        try {
            val querySnapshot = fireStore.collection("followLinks")
                .whereEqualTo("userBeingFollowed", followLink.userBeingFollowedEmail)
                .get()
                .await()
            for (document in querySnapshot.documents) {
                if (document.getString("userFollowing") == followLink.userFollowingEmail) {
                    document.reference.delete().await()
                }
            }
            return true
        } catch (e: Exception) {
            Log.d("poly", e.message.toString())
            e.printStackTrace()
        }
        return false
    }

    override suspend fun removeAllFollowLinkRelatedToUser(email: String): Boolean {
        try {
            val batch = fireStore.batch()
            val followers = fireStore
                .collection("followLinks")
                .whereEqualTo("userBeingFollowed", email)
                .get()
                .await()
            for (document in followers.documents) {
                batch.delete(document.reference)
            }
            val following = fireStore
                .collection("followLinks")
                .whereEqualTo("userFollowing", email)
                .get()
                .await()
            for (document in following.documents) {
                batch.delete(document.reference)
            }
            batch.commit().await()
            return true
        } catch (e: Exception) {
            Log.d("poly", e.message.toString())
            e.printStackTrace()
        }
        return false
    }
}